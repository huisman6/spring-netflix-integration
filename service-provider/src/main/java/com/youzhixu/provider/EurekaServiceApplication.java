package com.youzhixu.provider;

import java.util.List;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dooioo.se.lorik.core.annotation.EnableBuiltinRestSupport;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;

/**
 * @author huisman
 * @createAt 2015年9月16日 下午2:36:08
 * @since 1.0.0
 * @Copyright (c) 2015, Youzhixu.com All Rights Reserved.
 */
@SpringBootApplication
@Configuration
@EnableEurekaClient
@EnableBuiltinRestSupport
public class EurekaServiceApplication {
	@RequestMapping("/")
	public String home() {
		DiscoveryClient dc = DiscoveryManager.getInstance().getDiscoveryClient();
		Applications a = dc.getApplications();
		List<Application> ap = a.getRegisteredApplications();
		for (Application app : ap) {
			List<InstanceInfo> ins = dc.getInstancesByVipAddress(app.getName(), false);
			for (InstanceInfo instanceInfo : ins) {
				System.out.println("appName:" + instanceInfo.getAppName() + ",ipaddr="
						+ instanceInfo.getIPAddr() + ",hostname=" + instanceInfo.getHostName()
						+ ",port=" + instanceInfo.getPort() + ",vipAddress="
						+ instanceInfo.getVIPAddress() + ",status=" + instanceInfo.getStatus());
			}
		}
		return "Hello world";
	}

	public static void main(String[] args) {
		new SpringApplicationBuilder(EurekaServiceApplication.class).web(true).run(args);
	}
}
