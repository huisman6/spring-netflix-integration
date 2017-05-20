package com.netflix.eureka.server.filter.response;


import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.eureka.server.EurekaServerConfiguration;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.provider.DiscoveryJerseyProvider;
import com.youzhixu.registry.eureka.transform.ServerCodecProxy;


/**
 * Spring {@code EurekaServerConfiguration#EUREKA_PACKAGES} 只扫描”com.netflix.eureka“开头的报名。
 * 我们拦截所有response里包含InstanceInfo的响应结果（在转换为json/xml之前修改）
 * 
 * 尽量不要把provider放在静态变量里，防止类加载顺序引起的静态代码块初始化问题。
 * 
 * Jersey1.x需要实现MessageBodyWriter，注意SecurtityCheckResponseFilter因为是具体类型，所有优先级高。
 * jersy2.x就可以采用ContainerResponseFilter
 * @see EurekaServerConfiguration
 */
@Provider
@Produces({"application/json", "application/xml"})
@Consumes("*/*")
public class SecurtityCheckResponseFilter
		implements javax.ws.rs.ext.MessageBodyWriter<InstanceInfo> {
	private static final Logger logger=LoggerFactory.getLogger(SecurtityCheckResponseFilter.class);
	private final DiscoveryJerseyProvider provider;

	public SecurtityCheckResponseFilter() {
		provider = new DiscoveryJerseyProvider();
	}

	@Override
	public boolean isWriteable(Class<?> serializableClass, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return this.provider.isWriteable(serializableClass, genericType, annotations, mediaType);
	}

	@Override
	public long getSize(InstanceInfo serializableObject, Class<?> serializableClass,
			Type genericType, Annotation[] annotations, MediaType mediaType) {
		return this.provider.getSize(serializableObject, serializableClass, genericType,
				annotations, mediaType);
	}

	@Override
	public void writeTo(InstanceInfo instance, Class<?> serializableClass, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> headers,
			OutputStream outputStream) throws IOException, WebApplicationException {
		// 偷梁换柱
		logger.info("===========>正在过滤instance: app={},id={}",instance.getVIPAddress(),instance.getId());
		InstanceInfo serializableObject = ServerCodecProxy.getSerializableInstanceInfo(instance);
		this.provider.writeTo(serializableObject, serializableClass, genericType, annotations,
				mediaType, headers, outputStream);
	}



}


