package com.youzhixu.consumer;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lianjia.springremoting.imp.eureka.config.EurekaRPCInvokerConfig;
import com.lianjia.springremoting.invoker.annotation.Remoting;
import com.youzhixu.api.service.CityService;
import com.youzhixu.api.service.UserService;

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
@RestController
@EnableEurekaClient
@Import(EurekaRPCInvokerConfig.class)
public class ConsumerApplication {

	@Remoting
	CityService cityService;
	@Remoting
	UserService userService;

	@RequestMapping(value = "/users")
	public Object searchAll(HttpServletRequest request) {
		return userService.findAll();
	}

	@RequestMapping(value = "/user/{id}")
	public Object searchAll(@PathVariable(value = "id") int id) {
		return userService.findById(id);
	}


	@RequestMapping(value = "/search")
	public Object test(HttpServletRequest request) {
		String ids = request.getParameter("ids");
		if (ids == null || ids.trim().isEmpty()) {
			return null;
		}
		String[] idArrs = ids.split(",");
		List<Integer> list = new ArrayList<>(idArrs.length);
		for (int i = 0; i < idArrs.length; i++) {
			list.add(Integer.parseInt(idArrs[i]));
		}
		return cityService.findList(list);
	}

	public static void main(String[] args) {
		SpringApplication.run(ConsumerApplication.class, args);
	}
}
