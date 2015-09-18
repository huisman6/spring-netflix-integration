package com.youzhixu.api.service;

import java.util.List;

import com.youzhixu.api.model.City;
import com.youzhixu.springremoting.exporter.annotation.HessianService;

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
@HessianService(app = "city")
public interface CityService {
	List<City> findList(List<Integer> ids);

	boolean updateById(int id);
}
