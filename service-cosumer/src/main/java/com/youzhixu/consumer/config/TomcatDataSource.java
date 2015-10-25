package com.youzhixu.consumer.config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.aop.framework.ProxyFactory;

/**
 * <p>
 * 
 * </p> 
 * @author huisman 
 * @since 1.0.0
 * @createAt 2015年10月24日 下午8:16:25
 * @Copyright (c) 2015,Youzhixu.com Rights Reserved. 
 */
public class TomcatDataSource implements InvocationHandler{
	private Object dataSource;
	private Set<String> methods;
	public TomcatDataSource(Object dataSource,String interceptorMethod,String... optionalInterceptorMethods) {
		super();
		if (dataSource== null) {
			throw new IllegalArgumentException("dataSource is null");
		}
		if (interceptorMethod == null || interceptorMethod.trim().isEmpty()) {
			throw new IllegalArgumentException("interceptorMethod is null");
		}
		this.dataSource = dataSource;
		methods=new HashSet<>(2);
		methods.add(interceptorMethod);
		if (optionalInterceptorMethods !=null) {
			methods.addAll(Arrays.asList(optionalInterceptorMethods));
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getReturnType() ==void.class&&Modifier.isPublic(method.getModifiers()) && !method.isSynthetic() 
				&&methods.contains(method.getName())) {
			//我们只拦截public 的setter方法
			System.out.println(method.getName()+",vars="+Arrays.toString(args));
			return method.invoke(dataSource, "test===========>>");
		}
		return method.invoke(dataSource, args);
	}
}


