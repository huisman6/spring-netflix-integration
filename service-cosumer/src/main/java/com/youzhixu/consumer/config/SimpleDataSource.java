package com.youzhixu.consumer.config;

import java.util.Properties;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;


/**
 * <p>
 * 
 * </p> 
 * @author huisman 
 * @since 1.0.0
 * @createAt 2015年10月24日 下午9:51:06
 * @Copyright (c) 2015,Youzhixu.com Rights Reserved. 
 */
public class SimpleDataSource extends DataSource{

	@Override
	public void setPoolProperties(PoolConfiguration poolProperties) {
		super.setPoolProperties(poolProperties);
	}

	@Override
	public void setPassword(String password) {
		System.out.println("password="+password+",change to ===>>111222");
		super.setPassword("111222");
	}

	@Override
	public void setDbProperties(Properties dbProperties) {
		super.setDbProperties(dbProperties);
	}
 
}


