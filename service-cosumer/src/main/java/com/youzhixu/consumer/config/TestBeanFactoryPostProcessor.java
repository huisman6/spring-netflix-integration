package com.youzhixu.consumer.config;

import java.beans.PropertyDescriptor;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Proxy;
import java.security.ProtectionDomain;

import javax.sql.DataSource;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.context.weaving.LoadTimeWeaverAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.util.ClassUtils;

/**
 * <p>
 *  此类必须在其他postProcessor之前调用
 * </p> 
 * @author huisman 
 * @since 1.0.0
 * @createAt 2015年10月24日 上午8:43:22
 * @Copyright (c) 2015,Youzhixu.com Rights Reserved. 
 */
public class TestBeanFactoryPostProcessor extends InstantiationAwareBeanPostProcessorAdapter 
implements BeanClassLoaderAware,PriorityOrdered{
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
	private ClassLoader classLoader;
	
	@Override
	public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds,
			Object bean, String beanName) throws BeansException {
		return super.postProcessPropertyValues(pvs, pds, bean, beanName);
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		if (bean instanceof DataSource) {
			return Proxy.newProxyInstance(this.classLoader, ClassUtils.getAllInterfaces(bean),
					new TomcatDataSource(bean, "setPassword", "setUsername","setUserName"));
		}
		return super.postProcessBeforeInitialization(bean, beanName);
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		
		this.classLoader=classLoader;
	}
}


