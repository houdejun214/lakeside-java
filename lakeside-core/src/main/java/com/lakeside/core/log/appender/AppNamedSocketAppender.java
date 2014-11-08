package com.lakeside.core.log.appender;

import ch.qos.logback.classic.net.SocketAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.Map;

public class AppNamedSocketAppender extends SocketAppender {
	private static final String DEFAULT_APPNAME = "none";
	private String app=DEFAULT_APPNAME;
	
	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	@Override
	protected void postProcessEvent(ILoggingEvent event) {
	   Map<String, String> pros = event.getMDCPropertyMap();
	   if(pros!=null){
		   pros.put("app", app);
	   }
	   super.postProcessEvent(event);
	}
}
