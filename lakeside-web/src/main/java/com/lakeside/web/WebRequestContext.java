package com.lakeside.web;

import javax.servlet.http.HttpServletRequest;

/**
 * @author houdj
 *
 */
public class WebRequestContext {

	private static final ThreadLocal<WebRequestContext> context = new ThreadLocal<WebRequestContext>();

	private HttpServletRequest request;
	
	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	/**
	 * get the context instance;
	 * 
	 * @return
	 */
	public static WebRequestContext getContext() {
		WebRequestContext ctx = context.get();
		if (ctx == null) {
			throw new RuntimeException(
					"web request context object hava not be initialized");
		}
		return ctx;
	}
	
	/**
	 * get the context path of request
	 * @return
	 */
	public static String getContextPath(){
		WebRequestContext ctx = getContext();
		if (ctx == null) {
			return "";
		}
		HttpServletRequest request=ctx.getRequest();
		if(request==null){
			return "";
		}
		return request.getContextPath();
	}

	/**
	 * clear context object
	 */
	public static void clearContext() {
		context.remove();
	}

	/**
	 * set context object
	 * @param request
	 */
	public static void initialize(HttpServletRequest request) {
		WebRequestContext requestContext = new WebRequestContext();
		requestContext.setRequest(request);
		context.set(requestContext);
	}
}
