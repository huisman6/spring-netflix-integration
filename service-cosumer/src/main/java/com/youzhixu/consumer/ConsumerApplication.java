package com.youzhixu.consumer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

import com.lianjia.microservice.netflix.feign.FeignClientsScan;
import com.lianjia.se.governance.core.annotation.EnableBuiltinRestSupport;
import com.lianjia.se.governance.core.annotation.EnableEndpoints;
import com.lianjia.se.governance.core.annotation.EnableReconfigure;
import com.lianjia.springremoting.imp.eureka.config.EurekaRPCInvokerConfig;
import com.youzhixu.consumer.feign.FeignClientService;

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
@SpringBootApplication
@Import(EurekaRPCInvokerConfig.class)
@EnableEndpoints
@EnableReconfigure
@EnableBuiltinRestSupport(enableCORS = true)
@FeignClientsScan(basePackageClasses = FeignClientService.class)
public class ConsumerApplication {

	public static void main(String[] args) {
		// FeignClientsConfig
		// ViewSupportFeinFactoryBean
		// ConfigurationClassPostProcessor
		// AutowiredAnnotationBeanPostProcessor
		new SpringApplicationBuilder(ConsumerApplication.class).showBanner(true).build().run(args);
	}
}
