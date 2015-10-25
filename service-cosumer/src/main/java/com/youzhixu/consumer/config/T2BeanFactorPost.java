package com.youzhixu.consumer.config;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

/**
 * <p>
 * </p> 
 * @author huisman 
 * @since 1.0.0
 * @createAt 2015年10月24日 下午9:53:53
 * @Copyright (c) 2015,Youzhixu.com Rights Reserved. 
 */
public class T2BeanFactorPost implements BeanFactoryPostProcessor,ApplicationContextAware {

	
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
			throws BeansException {
		
		String[] beanNames=beanFactory.getBeanNamesForType(DataSource.class);
		if (beanNames != null) {
		  for (String beanName : beanNames) {
			  AbstractBeanDefinition bd=  (AbstractBeanDefinition)beanFactory.getBeanDefinition(beanName);
			  //应该检查是否是FactoryBean,Spring将使用@Bean的方法包装为FactoryMethod，对于这类bean我们应该重写其FactoryClass
			  String method = bd.getFactoryMethodName();
				String bean = bd.getFactoryBeanName();
			  if (method !=null && bean !=null) {
				//
				  bd.getFactoryBeanName();
			}
			 //指定子类类型
			  bd.setBeanClass(SimpleDataSource.class);
			  bd.setBeanClassName(SimpleDataSource.class.getName());
			 System.out.println(bd.getBeanClassName());
		  }
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ConfigurableEnvironment mutable = (ConfigurableEnvironment) applicationContext
				.getEnvironment();
			MutablePropertySources mps= mutable.getPropertySources();
		   Iterator<PropertySource<?>> its= mps.iterator();
		   while (its.hasNext()) {
			   PropertySource<?> p= its.next();
			   Object target=p.getSource();
			   //only check properies 文件
			   if (!(target instanceof Properties)) {
				 continue;
			   }
//			   if (mutable instanceof StandardEnvironment) {
//				 if (StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME.equals(p.getName())
//						 ||StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME.equals(p.getName())
//						 || StandardEnvironment.DEFAULT_PROFILES_PROPERTY_NAME.equals(p.getName())) {
//					continue;
//				}
//			   }
			   System.out.println(p.getName()+",target="+p.getSource().getClass());;
			   modifyEncryedText((Properties)target);
		}
	}
	
	private void modifyEncryedText(Properties source){
		if (source == null) {
			return;
		}
		
	    Enumeration<?> propertyNames=source.propertyNames();
	    Set<String> optionalDriverName=new HashSet<>();
	    optionalDriverName.add("driverClass");
	    optionalDriverName.add("driverclass");
	    optionalDriverName.add("driverclassname");
	    optionalDriverName.add("driverClassName");
	    Map<String,String> driverMap=new HashMap<>();
	    while(propertyNames.hasMoreElements()){
	    	String key=(String)propertyNames.nextElement();
	    	for (String driver : optionalDriverName) {
				if (key.endsWith(driver)) {
					//if found,check if class
					driverMap.put(key, driver);
				}
			}
	    }
	    if (driverMap.isEmpty()) {
			return ;
		}
	    //found , and  deduce prefix
	    for (Entry<String,String> pair : driverMap.entrySet()) {
	    	String passwordKey="password";
	    	if (pair.getKey().length() > pair.getValue().length()) {
	    		passwordKey=pair.getKey().substring(0,pair.getKey().length()-pair.getValue().length())+passwordKey;
	    	}
	    	String password=source.getProperty(passwordKey);
	    	System.out.println("============>> found:suffix="+pair.getValue()+",passwordKey:"+passwordKey+",password="+password);
	    	source.put(passwordKey, "decrypted:"+password);
		}
	}

}


