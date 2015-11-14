package com.youzhixu.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lianjia.springremoting.invoker.annotation.Remoting;
import com.youzhixu.api.service.CityService;
import com.youzhixu.api.service.UserService;
import com.youzhixu.consumer.feign.FeignClientService;

/**
 * <p>
 *
 * </p>
 * 
 * @author huisman
 * @since 1.0.0
 * @Copyright (c) 2015, Lianjia Group All Rights Reserved.
 */
@RestController
public class RestControllerTest {
	@Autowired
	FeignClientService feignClientService;
	@Remoting
	CityService cityService;

	@Remoting
	UserService userService;

	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public Object test() {
		return feignClientService.digest();
	}

	@RequestMapping(value = "/digest", method = RequestMethod.GET)
	public Object digest() {
		return userService.findAll();
	}
}
