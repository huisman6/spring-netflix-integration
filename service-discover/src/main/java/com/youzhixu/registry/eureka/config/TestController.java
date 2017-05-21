package com.youzhixu.registry.eureka.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;


/**
 * 
 * @author huisman 
 * @since 1.0.10
 * @Copyright (c) 2017,Youzhixu.com Rights Reserved. 
 */
@RestController
public class TestController {
 @Autowired
 private PeerAwareInstanceRegistry discoveryClient;
 
  @RequestMapping("/apps")
  public Object test(){
	  return this.discoveryClient.getApplications();
  }
}


