package com.youzhixu.consumer.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceConfig {
	/**
	 * 楼盘普通数据源
	 * 
	 * @return
	 */
	@Primary
	// 主数据源，必须配置，spring启动时会执行初始化数据操作（无论是否真的需要），选择查找DataSource class类型的数据源
	@Bean(name = "dataSource")
	@ConfigurationProperties(prefix = "loupan.datasource.common")
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	/**
	 * 楼盘只读数据源
	 * 
	 * @return
	 */
	@Bean(name = "dataSourceReadOnly")
	@ConfigurationProperties(prefix = "loupan.datasource.readOnly")
	public DataSource dataSourceReadOnly() {
//		PropertyOverrideConfigurer 
//		PropertyPlaceholderConfigurer
//		org.apache.tomcat.jdbc.pool.DataSource
		// ConfigurationPropertiesBindingPostProcessor
		return DataSourceBuilder.create().build();
	}
//	@Bean
//	public static T2BeanFactorPost processor(){
//		return new T2BeanFactorPost();
//	}
}