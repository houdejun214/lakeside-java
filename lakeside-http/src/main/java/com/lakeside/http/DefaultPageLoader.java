package com.lakeside.http;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 *
 */
public class DefaultPageLoader extends HttpPageLoader {

	@Override
	protected HttpClient getHttpClient() {
		return HttpClients.createSystem();
	}

}
