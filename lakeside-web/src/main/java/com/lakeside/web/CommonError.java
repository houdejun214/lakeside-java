/*
 * CommonError.java        2010-11-17
 * Copyright (c) 2010 nycticebus team.
 * All rights reserved.
 */

package com.lakeside.web;

/**
 * 
 * @author Dejun Hou
 */
public class CommonError {
	/**
	 * 请求路径地址
	 */
	private String requestPath;
	/**
	 * 异常消息
	 */
	private String exceptionMessage;

	public String getRequestPath() {
		return requestPath;
	}

	public void setRequestPath(String requestPath) {
		this.requestPath = requestPath;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}

	public void setExceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}

}
