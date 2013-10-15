package com.lakeside.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextHolder {

	private static ApplicationContext applicationContext = null;

	private static Logger logger = LoggerFactory.getLogger(ApplicationContextHolder.class);

	/**
	 * 实现ApplicationContextAware接�?�, 注入Context到�?��?�?��?中.
	 */
	@Autowired
	public void setApplicationContext(ApplicationContext applicationContext) {
		logger.debug("注入ApplicationContext到SpringContextHolder:" + applicationContext);
		ApplicationContextHolder.applicationContext = applicationContext; //NOSONAR
	}

	/**
	 * 实现DisposableBean接�?�,在Context关闭时清�?��?��?�?��?.
	 */
	public void destroy() throws Exception {
		ApplicationContextHolder.cleanApplicationContext();

	}

	/**
	 * �?�得存储在�?��?�?��?中的ApplicationContext.
	 */
	public static ApplicationContext getApplicationContext() {
		checkApplicationContext();
		return applicationContext;
	}

	/**
	 * 从�?��?�?��?ApplicationContext中�?�得Bean, 自动转型为所赋值对象的类型.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name) {
		checkApplicationContext();
		return (T) applicationContext.getBean(name);
	}

	/**
	 * 从�?��?�?��?ApplicationContext中�?�得Bean, 自动转型为所赋值对象的类型.
	 * 如果有多个Bean符�?�Class, �?�出第一个.
	 */
	public static <T> T getBean(Class<T> requiredType) {
		checkApplicationContext();
		return applicationContext.getBean(requiredType);
	}

	/**
	 * 清除applicationContext�?��?�?��?.
	 */
	public static void cleanApplicationContext() {
		logger.debug("清除SpringContextHolder中的ApplicationContext:" + applicationContext);
		applicationContext = null;
	}

	/**
	 * 检查ApplicationContext�?为空.
	 */
	private static void checkApplicationContext() {
		if (applicationContext == null) {
			throw new IllegalStateException("applicaitonContext未注入,请在applicationContext.xml中定义SpringContextHolder");
		}
	}
}
