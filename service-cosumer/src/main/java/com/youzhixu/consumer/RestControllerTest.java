package com.youzhixu.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dooioo.se.lorik.core.web.result.Assert;
import com.youzhixu.api.model.User;
import com.youzhixu.api.service.CityService;
import com.youzhixu.api.service.UserService;

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
	private static int index = 0;
	private Class<?>[] models =
			new Class[] {User1.class, User2.class, User3.class, User4.class, User.class};
	@Autowired
	CityService cityService;

	@Autowired
	UserService userService;

	@RequestMapping(value = "/citys", method = RequestMethod.GET)
	public Object test() {
		return cityService.finds();
	}

	@RequestMapping(value = "/city/1", method = RequestMethod.GET)
	public Object city1() {
		return cityService.find();
	}

	@RequestMapping(value = "/users", method = RequestMethod.GET)
	public Object users(@RequestParam(value = "random", required = false) Integer random) {
		if (random != null && random % 2 == 0) {
			Assert.justDenied("e:" + random);
		}

		return userService.findAll().as(models[index++ % models.length]);
	}

	@RequestMapping(value = "/users/{id}", method = RequestMethod.GET)
	public Object user(@PathVariable(value = "id") int id) {
		if (id % 2 == 0) {
			Assert.justDenied("e:" + id);
		}
		return userService.findById(id).as(models[index++ % models.length]);
	}

	@RequestMapping(value = "/city/update", method = RequestMethod.POST)
	public Object updateCity() {
		return cityService.updateById(20);
	}

	static class User2 {
		private String userName;

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

	}

	static class User3 {
		private String userName;
		int id;


		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

	}

	static class User4 {
		private String userName;
		private String desc;

		public String getDesc() {
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

	}
	static class User1 {
		int id;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

	}

}
