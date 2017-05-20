package com.youzhixu.registry.eureka.transform;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.netflix.appinfo.EurekaAccept;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.PortType;
import com.netflix.discovery.converters.wrappers.CodecWrapper;
import com.netflix.discovery.converters.wrappers.EncoderWrapper;
import com.netflix.eureka.registry.Key.KeyType;
import com.netflix.eureka.registry.ResponseCache;
import com.netflix.eureka.registry.ResponseCacheImpl;
import com.netflix.eureka.resources.DefaultServerCodecs;
import com.netflix.eureka.resources.ServerCodecs;

/**
 * 我们替换JACKSON_JSON的目的是，不向客户端输出某些特定的涉及安全的instance metadata。
 * 
 * 目前，返回给客户端实例查询信息，Eureka是放在ResponseCache里， 然后使用
 * {@code ServerCodecs#getEncoder(com.netflix.eureka.registry.Key.KeyType, com.netflix.appinfo.EurekaAccept)}
 * 来编码缓存；
 * 
 * 目前（SpringCloud Brixton SR7) ServerCodecs#getEncoder只供ResponseCacheImpl调用。
 * 升级SpringCloud时，一定要留意此类的getEncoder系列方法是否用于复制instances给其他Eureka Server节点。
 * 我们只是不想向客户端输出metada，但不限制Eureka Server之间。
 * 
 * 当前类可以防止以下url泄漏安全token: /eureka/apps,/eureka/apps/{vip},/eureka/vips/{vip},/eureka/svips/{svip};
 * 
 * 但是不能防止查看实例信息的接口泄漏token：/eureka/instances/{instanceId},/eureka/apps/{vip}/{instanceId};
 * 
 * 可能存在更改token的接口： /eureka/apps/appID/instanceID/metadata;
 * 
 * 因此需要另一套逻辑来出来这些接口，单元测试需要覆盖到token是否泄漏的检测
 * 
 * 实现所有token安全功能时，必须测试是否会影响Eureka Server节点数据之间的复制，因为Eureka Server有时候也使用这些接口。
 * 
 * @see https://github.com/Netflix/eureka/wiki/Eureka-REST-operations
 * @see ResponseCache
 * @see ResponseCacheImpl
 * @see ServerCodecs
 */
public class ServerCodecProxy implements BeanPostProcessor {
	private final Logger logger = LoggerFactory.getLogger(ServerCodecProxy.class);
	private final static String META_CLIENT_TOKEN = "clientToken";
	
	/**
	  * 新生成一个copy，防止同一个引用被修改
	 */
	public static InstanceInfo  getSerializableInstanceInfo(InstanceInfo rawInstance){
		Map<String,String> metaMap=null;
		if (rawInstance.getMetadata() == null || rawInstance.getMetadata().isEmpty()) {
			metaMap=Collections.emptyMap();
		}else{
			metaMap=new HashMap<>(rawInstance.getMetadata());
			//移除
			metaMap.remove(META_CLIENT_TOKEN);
		}
		//我们过滤一些特殊字段，涉及到安全控制的 ，可能涉及引用，我们copy一份
		InstanceInfo proxy=
				 InstanceInfo.Builder.newBuilder()
			        .setActionType(rawInstance.getActionType())
			        .setAppGroupName(rawInstance.getAppGroupName())
			        .setInstanceId(rawInstance.getInstanceId())
			        .setAppName(rawInstance.getAppName())
			        
			        .setVIPAddress(rawInstance.getVIPAddress())
			        .setSecureVIPAddress(rawInstance.getSecureVipAddress())
			        .setHostName(rawInstance.getHostName())
			        .setIPAddr(rawInstance.getIPAddr())
			        
			        .setPort(rawInstance.getPort())
			        .setSecurePort(rawInstance.getSecurePort())
			        .enablePort(PortType.SECURE	
			        		, rawInstance.isPortEnabled(PortType.SECURE))
			        .enablePort(PortType.UNSECURE	
			        		, rawInstance.isPortEnabled(PortType.UNSECURE))
			        
			        .setMetadata(metaMap)
			        .setStatus(rawInstance.getStatus())
			        .setDataCenterInfo(rawInstance.getDataCenterInfo())
			        .setHealthCheckUrls(null,rawInstance.getHealthCheckUrl(),rawInstance.getSecureHealthCheckUrl())
			        .setHomePageUrl(null,rawInstance.getHomePageUrl())
			        .setStatusPageUrl(null,rawInstance.getStatusPageUrl())
			        .setLeaseInfo(rawInstance.getLeaseInfo())
			        .build();
		proxy.getMetadata().remove("clientToken");
		return proxy;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		if (bean instanceof FactoryBean) {
			// 我们只处理构造好的类型
			return bean;
		}
		if (bean instanceof ServerCodecs) {
			ServerCodecs oldCodes = (ServerCodecs) bean;
			logger.warn("Spring	CloudServerCodecs已经被替换为DelegateServerCodecs");
			return new DelegateServerCodecs(oldCodes.getFullJsonCodec(),
					oldCodes.getCompactJsonCodec(), oldCodes.getFullXmlCodec(),
					oldCodes.getCompactXmlCodecr());
		}
		return bean;
	}

	static class DelegateServerCodecs extends DefaultServerCodecs {
		//json响应
		private static final JacksonJsonCodecWrapper ONLY_FOR_RESPONSE_CACHE_JACKSON_JSON =
				new JacksonJsonCodecWrapper();
		// 生成xml响应内容
		private static final XmlStreamCodecWrapper ONLY_FOR_RESPONSE_CACHE_XML_STREAM =
				new XmlStreamCodecWrapper();

		/**
		 * @param fullJsonCodec
		 * @param compactJsonCodec
		 * @param fullXmlCodec
		 * @param compactXmlCodec
		 */
		DelegateServerCodecs(CodecWrapper fullJsonCodec, CodecWrapper compactJsonCodec,
				CodecWrapper fullXmlCodec, CodecWrapper compactXmlCodec) {
			super(fullJsonCodec, compactJsonCodec, fullXmlCodec, compactXmlCodec);
		}

		@Override
		public EncoderWrapper getEncoder(KeyType keyType, boolean compact) {
			switch (keyType) {
				case JSON:
					return compact ? compactJsonCodec : ONLY_FOR_RESPONSE_CACHE_JACKSON_JSON;
				case XML:
				default:
					return compact ? compactXmlCodec : ONLY_FOR_RESPONSE_CACHE_XML_STREAM;
			}
		}

		@Override
		public EncoderWrapper getEncoder(KeyType keyType, EurekaAccept eurekaAccept) {
			return super.getEncoder(keyType, eurekaAccept);
		}
	}
}


