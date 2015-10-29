package com.youzhixu.consumer;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <p>
 * 
 * </p>
 * 
 * @author huisman
 * @since 1.0.0
 * @createAt 2015年10月10日 下午10:46:50
 * @Copyright (c) 2015,Youzhixu.com Rights Reserved.
 */
@FeignClient("test")
public interface FeignClientService {
	@RequestMapping(value = "/digest")
	public String digest();
}
