package com.lakeside.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.http.HttpServletRequest;

public class LogableSimpleMappingExceptionResolver extends SimpleMappingExceptionResolver{
	
	protected final Logger log = LoggerFactory.getLogger("LogableExceptionResolver");

	@Override
	protected void logException(Exception ex, HttpServletRequest request) {
		String buildLogMessage = buildLogMessage(ex, request);
		if(!(ex instanceof RuntimeException)){
			log.warn(buildLogMessage, ex);
		}
	}
}
