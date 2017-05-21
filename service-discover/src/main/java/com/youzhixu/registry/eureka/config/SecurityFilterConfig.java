package com.youzhixu.registry.eureka.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.cloud.netflix.eureka.EurekaConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 
 * @author huisman
 * @since 1.0.10
 * @Copyright (c) 2017,Youzhixu.com Rights Reserved.
 */
@Configuration
public class SecurityFilterConfig {

	@Bean
	public FilterRegistrationBean securityFilterBean() {
		FilterRegistrationBean bean = new FilterRegistrationBean();
		bean.setFilter(new AclEurekaResourceFilter());
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		bean.addUrlPatterns(EurekaConstants.DEFAULT_PREFIX+"/apps/*");
		return bean;
	}
	
	static class AclEurekaResourceFilter implements Filter{
		private static final Logger logger=LoggerFactory.getLogger(AclEurekaResourceFilter.class); 

		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			 HttpServletRequest httpRequest=HttpServletRequest.class.cast(request);
			 String uri=httpRequest.getRequestURI();
			 //目前先写死路径
			 if (uri!=null && uri.endsWith("metadata")) {
				 logger.warn("access denied,client: ip={},port={},x-real-ip:{}"
						 , request.getRemoteAddr(),request.getRemotePort(),httpRequest.getHeader("X-Real-Ip"));
				 if (!response.isCommitted()) {
					 HttpServletResponse httpResponse=HttpServletResponse.class.cast(response);
					 httpResponse.resetBuffer();
					 httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "endpoint disabled");
					 return;
				 }
			}
			chain.doFilter(request, response);
		}

		@Override
		public void init(FilterConfig arg0) throws ServletException {}
		
		@Override
		public void destroy() {}
	}
}


