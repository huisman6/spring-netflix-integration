package com.youzhixu.consumer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Import;

import com.lianjia.microservice.governance.core.annotation.EnableEndpoints;
import com.lianjia.microservice.governance.core.annotation.EnableReconfigure;
import com.lianjia.microservice.netflix.feign.FeignClientsScan;
import com.lianjia.microservice.netflix.feign.Types;
import com.lianjia.springremoting.imp.eureka.config.EurekaRPCInvokerConfig;
import com.youzhixu.consumer.feign.FeignClientService;

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
@Import(EurekaRPCInvokerConfig.class)
@EnableEndpoints
@EnableEurekaClient
@EnableReconfigure
@FeignClientsScan(basePackageClasses = FeignClientService.class)
public class ConsumerApplication {

	public static void main(String[] args) {
		// FeignClientsConfig
		// ViewSupportFeinFactoryBean
		// ConfigurationClassPostProcessor
		// AutowiredAnnotationBeanPostProcessor
		new SpringApplicationBuilder(ConsumerApplication.class).showBanner(true).build().run(args);
	}

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
			System.out
					.println("type:" + RequestContext.get() + ",toString:" + Arrays.toString(args));
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

		List<? super M1> ha();

		List<? extends M1> haha();

		List<?> hehe();

		<T> List<T> wa();

		<T> T get();
	}

	static interface PTT {
		<T> List<T> finds();
	}

	static class MV1 extends M1 {

	}

	@SuppressWarnings("rawtypes")
	static void typeCheck(Type type) {
		// 如果是泛型参数 比如 <T> public T get(int id);
		// 否则检查实际返回类型
		Type resolvedType = null;
		if (type instanceof TypeVariable) {
			// 优先检查请求类型
			TypeVariable tv = (TypeVariable) type;
			// 如果不能确认类型，没有上限，如果有，我们默认返回上限对象啊！！！！！
			if (tv.getBounds().length == 1 && tv.getBounds()[0] != Object.class) {
				// 其次返回指定上限的泛型
				resolvedType = tv.getBounds()[0];
			}
		} else if (type instanceof ParameterizedType) {
			ParameterizedType ptype = (ParameterizedType) type;

			Type[] ats = ptype.getActualTypeArguments();
			if (ats.length == 1) {
				if (ats[0] instanceof TypeVariable) {
					TypeVariable tmp = (TypeVariable) ats[0];
					if (tmp.getBounds().length == 1 && tmp.getBounds()[0] != Object.class) {
						resolvedType = tmp.getBounds()[0];
					}
				} else {
					// 如果是Class类型等Java实际存在的类型
					resolvedType = ats[0];
				}
			}
		}
		if (resolvedType == null) {
			throw new IllegalArgumentException("ParameterizedType class=" + type.getClass()
					+ " 无法解析其实际返回类型。请调用view方法或者@Provide标注指定其默认返回类型或者指定其上限");
		}

		System.out.println("type resolved:" + resolvedType.getTypeName());

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

	public static void testGeneric() {
		try {
			Type tt = PT.class.getMethod("get").getGenericReturnType();
			ParameterizedType ptt = (ParameterizedType) Types.resolveGenericType(tt, null, null);
			System.out.println(ptt.getActualTypeArguments()[0]);
			Type t = PT.class.getMethod("finds").getGenericReturnType();
			typeCheck(t);
			ParameterizedType pt = (ParameterizedType) t;
			ParameterizedType ptl = (ParameterizedType) TypeProvider.class
					.getMethod("getType", Class.class).invoke(new TypeProvider(), MV1.class)
					.getClass().getGenericSuperclass();
			System.out.println(ptl.getActualTypeArguments()[0]);
			System.out.println(pt.getActualTypeArguments()[0].getClass());
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		THandler handler = new THandler();
		Test t1 = (Test) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
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
	}

	public static class Test2 {
		@SuppressWarnings("unused")
		public static void testGeneric2() {
			/** 不指定泛型的时候 */
			int i = Test2.add(1, 2); // 这两个参数都是Integer，所以T为Integer类型
			Number f = Test2.add(1, 1.2);// 这两个参数一个是Integer，以风格是Float，所以取同一父类的最小级，为Number
			Object o = Test2.add(1, "asd");// 这两个参数一个是Integer，以风格是Float，所以取同一父类的最小级，为Object

			// &nbsp;/**指定泛型的时候*/
			int a = Test2.<Integer>add(1, 2);// 指定了Integer，所以只能为Integer类型或者其子类
			// int b = Test2.<Integer>add(1, 2.2);// 编译错误，指定了Integer，不能为Float
			Number c = Test2.<Number>add(1, 2.2); // 指定为Number，所以可以为Integer和Float
		}

		// 这是一个简单的泛型方法
		public static <T> T add(T x, T y) {
			return y;
		}
	}
}
