package com.youzhixu.consumer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

import com.dooioo.se.lorik.core.annotation.EnableBuiltinRestSupport;

/**
 * <p>
 *
 * </p>
 * 
 * @author huisman
 * @createAt 2015年9月16日 下午4:07:24
 * @since 1.0.0
 * @Copyright (c) 2015, Youzhixu.com All Rights Reserved.
 */
@Configuration
@SpringBootApplication
@EnableEurekaClient
@EnableBuiltinRestSupport
@EnableFeignClients(basePackages = "com.youzhixu.api.service")
public class ConsumerApplication {

	public static void main(String[] args) {
		// FeignClientsConfig
		// ViewSupportFeinFactoryBean
		// ConfigurationClassPostProcessor
		// AutowiredAnnotationBeanPostProcessor
		new SpringApplicationBuilder(ConsumerApplication.class).build().run(args);
	}
}
