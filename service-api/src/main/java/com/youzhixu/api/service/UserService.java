package com.youzhixu.api.service;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dooioo.se.lorik.spi.view.BeanView;
import com.dooioo.se.lorik.spi.view.ListView;
import com.youzhixu.api.model.User;


/**
 * <p>
 * api-service interface
 * </p>
 * 
 * @author huisman
 * @createAt 2015年9月15日 下午3:05:22
 * @since 1.0.0
 * @Copyright (c) 2015, Youzhixu.com All Rights Reserved.
 */
@FeignClient("user")
public interface UserService {
	/**
	 * <p>
	 * 根据用户ID查找用户信息
	 * </p>
	 * 
	 * @since: 1.0.0
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/users/{id}", method = RequestMethod.GET)
	BeanView<User> findById(@PathVariable(value = "id") int userId);

	/**
	 * <p>
	 * 查找所有用户
	 * </p>
	 * 
	 * @since: 1.0.0
	 * @return
	 */
	@RequestMapping(value = "/users", method = RequestMethod.GET)
	ListView<User> findAll();
}
