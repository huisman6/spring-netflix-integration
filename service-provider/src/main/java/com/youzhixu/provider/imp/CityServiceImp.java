package com.youzhixu.provider.imp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.web.bind.annotation.RestController;

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
@RestController
public class CityServiceImp implements CityService {

	@Override
	public boolean updateById(int id) {
		return false;
	}

	@Override
	public City find() {
		return new City(00000, "null", 0, 0);
	}

	@Override
	public List<City> finds() {
		// just do it
		ThreadLocalRandom tlr = ThreadLocalRandom.current();
		int count = tlr.nextInt(3, 1000);
		List<City> userList = new ArrayList<>(count);
		userList.add(new City(count, "总共生成city：" + count, 0, 0));
		for (int i = 0; i < count; i++) {
			int id = tlr.nextInt(1, 999999);
			userList.add(new City(id, "名字啊：" + id, tlr.nextDouble(), tlr.nextDouble()));
		}
		return userList;
	}



}
