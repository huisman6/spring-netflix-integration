package com.youzhixu.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lianjia.se.governance.core.annotation.Authorized;
import com.lianjia.se.governance.core.web.result.Assert;
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

	@Authorized
	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public Object test() {
		Integer userCode = null;
		Assert.found(userCode != null, 300010, "工号不存在");
		return feignClientService.digest();
	}

	@RequestMapping(value = "/api/digest", method = RequestMethod.GET)
	public Object digest(@RequestParam(value = "random") int random) {
		if (random % 2 == 0) {
			throw new TestExcetpion("random is " + random);
		}
		return userService.findAll();
	}

	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	static class TestExcetpion extends RuntimeException {
		private static final long serialVersionUID = 1L;

		/**
		 * 
		 */
		public TestExcetpion() {
			super();
		}

		/**
		 * @param message
		 * @param cause
		 * @param enableSuppression
		 * @param writableStackTrace
		 */
		public TestExcetpion(String message, Throwable cause, boolean enableSuppression,
				boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}

		/**
		 * @param message
		 * @param cause
		 */
		public TestExcetpion(String message, Throwable cause) {
			super(message, cause);
		}

		/**
		 * @param message
		 */
		public TestExcetpion(String message) {
			super(message);
		}

		/**
		 * @param cause
		 */
		public TestExcetpion(Throwable cause) {
			super(cause);
		}

	}
}
