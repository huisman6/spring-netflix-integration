package com.youzhixu.api.service;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.youzhixu.api.model.City;

/**
 * <p>
 *
 * </p>
 * 
 * @author huisman
 * @createAt 2015年9月16日 下午3:15:01
 * @since 1.0.0
 * @Copyright (c) 2015,Youzhixu.com All Rights Reserved.
 */
@FeignClient("city")
public interface CityService {
	@RequestMapping(value = "/city/update", method = RequestMethod.GET)
	boolean updateById(int id);

	@RequestMapping(value = "/city/1", method = RequestMethod.GET)
	City find();

	@RequestMapping(value = "/citys", method = RequestMethod.GET)
	List<City> finds();
}
