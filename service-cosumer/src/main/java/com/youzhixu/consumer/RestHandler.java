package com.youzhixu.consumer;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
public class RestHandler {
	@RequestMapping(value = "/digest")
	public Object test() {
		return "ok";
	}
}
