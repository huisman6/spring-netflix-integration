package com.youzhixu.registry.eureka.transform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.converters.Converters;
import com.netflix.discovery.converters.Converters.InstanceInfoConverter;
import com.netflix.discovery.converters.wrappers.CodecWrapper;
import com.netflix.discovery.converters.wrappers.CodecWrappers;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;

/**
 * 负责生成xml（application/xml)的响应内容，仅用在{@code ServerCodecProxy#postProcessAfterInitialization}
 */
@SuppressWarnings("all")
class XmlStreamCodecWrapper implements CodecWrapper {
	private final SecurityXmlStream codec = SecurityXmlStream.getInstance();

	@Override
	public String codecName() {
		return CodecWrappers.getCodecName(this.getClass());
	}

	@Override
	public boolean support(MediaType mediaType) {
		return mediaType.equals(MediaType.APPLICATION_XML_TYPE);
	}

	@Override
	public <T> String encode(T object) throws IOException {
		return codec.toXML(object);
	}

	@Override
	public <T> void encode(T object, OutputStream outputStream) throws IOException {
		codec.toXML(object, outputStream);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T decode(String textValue, Class<T> type) throws IOException {
		return (T) codec.fromXML(textValue, type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T decode(InputStream inputStream, Class<T> type) throws IOException {
		return (T) codec.fromXML(inputStream, type);
	}

	static class SecurityXmlStream extends XStream {
		private static final SecurityXmlStream instance = new SecurityXmlStream();

		private SecurityXmlStream() {
			super(new DomDriver(null, initializeNameCoder()));
			registerConverter(new Converters.ApplicationConverter());
			registerConverter(new Converters.ApplicationsConverter());
			registerConverter(new Converters.DataCenterInfoConverter());
			registerConverter(new InstanceXmlConverter());
			registerConverter(new Converters.LeaseInfoConverter());
			registerConverter(new Converters.MetadataConverter());
			setMode(XStream.NO_REFERENCES);
			processAnnotations(
					new Class[] {InstanceInfo.class, Application.class, Applications.class});
		}

		public static SecurityXmlStream getInstance() {
			return instance;
		}

		private static XmlFriendlyNameCoder initializeNameCoder() {
			@SuppressWarnings("deprecation")
			EurekaClientConfig clientConfig =
					DiscoveryManager.getInstance().getEurekaClientConfig();
			if (clientConfig == null) {
				return new XmlFriendlyNameCoder();
			}
			return new XmlFriendlyNameCoder(clientConfig.getDollarReplacement(),
					clientConfig.getEscapeCharReplacement());
		}
	}

	static class InstanceXmlConverter extends InstanceInfoConverter {
		private final static Logger logger=LoggerFactory.getLogger(InstanceXmlConverter.class);
		@Override
		public void marshal(Object source, HierarchicalStreamWriter writer,
				MarshallingContext context) {
			if (source instanceof InstanceInfo) {
				InstanceInfo origin=InstanceInfo.class.cast(source);
				source = ServerCodecProxy.getSerializableInstanceInfo(origin);
				logger.info("===========>正在过滤instance: app={},id={}"+origin.getVIPAddress(),origin.getId());

			}
			super.marshal(source, writer, context);
		}
	}


}


