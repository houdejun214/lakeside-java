/*
 * LocalMessage.java        2010-11-14
 * Copyright (c) 2010 nycticebus team.
 * All rights reserved.
 */

package com.lakeside.web;

import com.lakeside.core.utils.StringUtils;
import org.springframework.context.MessageSource;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * 本地资源消息处理类
 * 
 * @author Dejun Hou
 */

public class Message {

	/**
	 * 消息源对象，用来搜索消息。
	 */
	public static MessageSource messageSource = ApplicationContextHolder.getBean(MessageSource.class);

	/**
	 * 根据名称获取消息，如果未找到该名称对应的消息，返回该名称！
	 * @param code 	消息名称
	 * @param locale	消息对应的区域
	 * @param args	消息格式化参数
	 * @return
	 */
	public static String getMessage(String code, Locale locale, Object... args) {
		String message = null;
		if (StringUtils.isEmpty(code)) {
			return code;
		}
		if (messageSource == null) {
			return code;
		}
		Locale localeUsed = locale;
		if (localeUsed == null) {
			localeUsed = Locale.getDefault();
		}
		Object[] transedArgs = null;
		if (args != null && args.length != 0) {
			transedArgs = transArgs(args, localeUsed);
		}
		message = messageSource.getMessage(code, transedArgs, code, localeUsed);
		if (message.equals(code) && transedArgs != null) {
			message = MessageFormat.format(code, transedArgs);
		}
		return message;
	}

	public static String getMessage(String code) {
		return getMessage(code, null);
	}

	private static Object[] transArgs(Object[] args, Locale locale) {
		if (args == null) {
			return null;
		}
		if (args.length == 0) {
			return null;
		}
		if (messageSource == null) {
			return args;
		}
		Object[] result = new Object[args.length];
		for (int i = 0; i < result.length; i++) {
			if (args[i] instanceof String) {
				result[i] = messageSource.getMessage(String.valueOf(args[i]), null, String.valueOf(args[i]), locale);
			} else {
				result[i] = args[i];
			}
		}
		return result;
	}
}
