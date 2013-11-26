package com.lakeside.download.http;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * @author zhufb
 * 
 */
public class DefaultPageLoader extends HttpPageLoader {

	@Override
	protected HttpClient getHttpClient() {
		return HttpClients.createSystem();
	}

}
