package com.youzhixu.registry.eureka;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.Bean;

import com.youzhixu.registry.eureka.transform.ServerCodecProxy;

/**
 * <p>
 * eureca服务注册与发现
 * </p>
 * 
 * @author huisman
 * @createAt 2015年9月16日 上午10:52:21
 * @since 1.0.0
 * @Copyright (c) 2015, Youzhixu.com All Rights Reserved.
 */
@SpringBootApplication
@EnableEurekaServer
@EnableDiscoveryClient
public class EurekaServerApplication {
	
	@Bean
	public static ServerCodecProxy serverCodecProxy(){
		return new ServerCodecProxy();
	}
	
	public static void main(String[] args) {
		new SpringApplicationBuilder(EurekaServerApplication.class).web(true).run(args);
	}
}
