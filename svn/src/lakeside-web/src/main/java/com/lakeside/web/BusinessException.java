/*
 * BusinessException.java        2010-11-14
 * Copyright (c) 2010 nycticebus team.
 * All rights reserved.
 */
package com.lakeside.web;

import java.util.Locale;


/**
 * 自定义业务异常
 * 
 * @author Dejun Hou
 */
public class BusinessException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 消息名称
	 */
	private String code;
	/**
	 * 格式化参数
	 */
	private Object[] args;

	/**
	 * 业务异常构造器
	 * @param code 异常消息内容或消息代码名称
	 * @param args	异常消息格式化参数
	 */
	public BusinessException(String code, Object... args) {
		this.code = code;
		this.args = args;
	}

	/**
	 * 获取格式化参数参数
	 * @return
	 */
	public Object[] getArgs() {
		return this.args;
	}

	/**
	 * 消息名称
	 * @return
	 */
	public String getCode() {
		return this.code;
	}

	@Override
	public String getMessage() {
		return Message.getMessage(code, null, args);
	}

	public String getMessage(Locale locale) {
		return Message.getMessage(code, locale, args);
	}
}
