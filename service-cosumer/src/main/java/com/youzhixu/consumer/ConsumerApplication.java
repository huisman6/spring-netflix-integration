package com.youzhixu.consumer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lianjia.microservice.governance.core.annotation.EnableEndpoints;
import com.lianjia.microservice.governance.core.annotation.EnableReconfigure;
import com.lianjia.springremoting.imp.eureka.config.EurekaRPCInvokerConfig;
import com.lianjia.springremoting.invoker.annotation.Remoting;
import com.youzhixu.api.service.CityService;
import com.youzhixu.api.service.UserService;

/**
 * <p>
 *
 * </p>
 * 
 * @author huisman
 * @createAt 2015年9月16日 下午4:07:24
 * @since 1.0.0
 * @Copyright (c) 2015, Youzhixu.com All Rights Reserved.
 */
@SpringBootApplication
@RestController
@EnableEurekaClient
@Import(EurekaRPCInvokerConfig.class)
@EnableEndpoints
@EnableReconfigure
public class ConsumerApplication {

	@Remoting
	CityService cityService;
	@Remoting
	UserService userService;
	@Autowired(required = false)
	FeignClientService feignClientService;

	public static interface Test extends BeanView<Test> {
		String invoke(String str);

	}
	public static interface BeanView<T> {
		@SuppressWarnings("unchecked")
		default T to(Object obj) {
			System.out.println(this.getClass());
			return (T) this;
		}
	}

	static class T1 implements Test {
		@Override
		public String invoke(String str) {
			System.out.println("sddds:" + str);
			return str;
		}

	}

	static class Invoker {
		Object invoke(Object[] args) {
			System.out.println("type:" + RequestContext.get() + ",toString:"
					+ Arrays.toString(args));
			return Arrays.toString(args);
		}
	}

	static class RequestContext {
		private static ThreadLocal<Class<?>> types = new ThreadLocal<Class<?>>();

		public static void set(Class<?> type) {
			types.set(type);
		}

		public static Class<?> get() {
			return types.get();
		}
	}

	static class THandler implements InvocationHandler {


		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.isDefault() && BeanView.class.isAssignableFrom(method.getDeclaringClass())) {
				RequestContext.set(args[0].getClass());
				return proxy;
			}
			System.out.println("method:" + method.getName() + ",type=" + RequestContext.get()
					+ ",thread is:" + Thread.currentThread().getId());
			return new Invoker().invoke(args);
		}

	}
	static class M1 {

	}
	static interface PT {
		<T extends M1> List<T> finds();
	}

	static class MV1 extends M1 {

	}

	static class TypeProvider {
		public List<?> getType(Class<?> type) {
			List<Object> types = new ArrayList<>();
			try {
				types.add(type.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
			return types;
		}
	}

	public static void main(String[] args) {
		try {
			Type t = PT.class.getMethod("finds").getGenericReturnType();
			ParameterizedType pt = (ParameterizedType) t;
			ParameterizedType ptl =
					(ParameterizedType) TypeProvider.class.getMethod("getType", Class.class)
							.invoke(new TypeProvider(), MV1.class).getClass()
							.getGenericSuperclass();
			System.out.println(ptl.getActualTypeArguments()[0]);
			System.out.println(pt.getActualTypeArguments()[0].getClass());
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		THandler handler = new THandler();
		Test t1 =
				(Test) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
						new Class[] {Test.class}, handler);
		// t1.to(new Object()).invoke("...");
		Object[] targets = new Object[] {"", Long.valueOf(0), Integer.valueOf(3)};
		Random random = new Random();
		for (Object object : targets) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					if (random.nextInt() % 2 == 0) {
						t1.to(object).invoke("hahahah - 等于0");
					} else {
						t1.invoke("哈哈 -----乘以2====》");
					}
				}
			}).start();

		}
		// SpringApplication.run(ConsumerApplication.class, args);
	}

	@RequestMapping(value = "/users")
	public Object searchAll(HttpServletRequest request) {
		return userService.findAll();
	}

	@RequestMapping(value = "/user/{id}")
	public Object searchAll(@PathVariable(value = "id") int id) {
		return userService.findById(id);
	}


	@RequestMapping(value = "/search")
	public Object test(HttpServletRequest request) {
		String ids = request.getParameter("ids");
		if (ids == null || ids.trim().isEmpty()) {
			return null;
		}
		String[] idArrs = ids.split(",");
		List<Integer> list = new ArrayList<>(idArrs.length);
		for (int i = 0; i < idArrs.length; i++) {
			list.add(Integer.parseInt(idArrs[i]));
		}
		return cityService.findList(list);
	}
}
