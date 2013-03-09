/*
 * BaseController.java        2010-11-17
 * Copyright (c) 2010 nycticebus team.
 * All rights reserved.
 */

package com.lakeside.web;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import com.lakeside.core.utils.StringUtils;

/**
 * Controller 基类
 * @author Dejun Hou
 */
public abstract class BaseController {
	
	protected final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
	
	protected static final String MESSAGE_TYPE_SUCCESS = "success";
	protected static final String MESSAGE_TYPE_ERROR = "error";
	protected static final String MESSAGE_TYPE_NOTICE = "notice";
	protected static final String MESSAGE_ATTRIBUTE = "message";
	protected static final String MESSAGE_TYPE_ATTRIBUTE = "message_type";
	

	protected String encodeUrlPathSegment(String pathSegment, HttpServletRequest request) {
	    String enc = request.getCharacterEncoding();
	    if (enc == null) {
	        enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
	    }
	    try {
	        pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
	    }
	    catch (UnsupportedEncodingException uee) {}
	    return pathSegment;
	}

	protected void putMessage(HttpServletRequest request, String message, String type) {
		request.setAttribute(MESSAGE_ATTRIBUTE, message);
		if(!StringUtils.isEmpty(type)){
			request.setAttribute(MESSAGE_TYPE_ATTRIBUTE, type);
		}
	}

	protected void putMessage(String message, String type) {
		this.putMessage(WebRequestContext.getContext().getRequest(), message, type);
	}

	protected void putSuccessMessage(String message) {
		this.putMessage(message, MESSAGE_TYPE_SUCCESS);
	}

	protected void putErrorMessage(String message) {
		this.putMessage(message,MESSAGE_TYPE_ERROR);
	}

	/**
	 * redirect the client to another url, client browser will reload the redirect url
	 * 
	 * @param redirectUrl
	 * @return
	 */
	protected String redirect(String redirectUrl) {
		return "redirect:"+redirectUrl;
	}

	/**
	 * forward the request to another url, this occur in the server side.
	 * 
	 * @param forwardUrl
	 * @return
	 */
	protected String forward(String forwardUrl) {
		return "forward:"+forwardUrl;
	}

	protected String getLink(HttpServletRequest request, String redirectUrl, String message) {
		String serverName = request.getServerName();
		int portNumber = request.getServerPort();
		String context  = request.getContextPath();
		if(!StringUtils.isEmpty(context)){
			if(!context.startsWith("/")){
				context="/"+context;
			}
			context=StringUtils.chompLast(context, "/");
		}else{
			context="";
		}
		String link="http://"+serverName+":"+portNumber+context+redirectUrl;
		return "<a href='"+link+"'>"+message+"</a>";
	}
}
