package com.youzhixu.api.service;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author huisman
 * @since 1.0.0
 * @createAt 2015年10月10日 下午10:46:50
 * @Copyright (c) 2015,Youzhixu.com Rights Reserved.
 */
@FeignClient(url = "http://localhost:10086")
public interface FeignClientService {
	@RequestMapping(value = "/digest")
	public String digest();
}
