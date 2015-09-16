package com.youzhixu.provider.imp;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.youzhixu.api.model.City;
import com.youzhixu.api.service.CityService;

/**
 * <p>
 *
 * </p>
 * 
 * @author huisman
 * @createAt 2015年9月16日 下午3:23:06
 * @since 1.0.0
 * @Copyright (c) 2015, Lianjia Group All Rights Reserved.
 */
@Service
public class CityServiceImp implements CityService {

	@Override
	public List<City> findList(List<Integer> ids) {
		if (ids == null || ids.isEmpty()) {
			return new ArrayList<City>(1);
		}
		List<City> cities = new ArrayList<>(ids.size());
		for (Integer id : ids) {
			cities.add(new City(id, "城市：" + id, id * Math.random(), id * Math.random()));
		}
		return cities;
	}

	@Override
	public boolean updateById(int id) {
		return false;
	}

}
