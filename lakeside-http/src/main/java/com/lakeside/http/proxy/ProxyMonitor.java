package com.lakeside.http.proxy;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class ProxyMonitor extends Thread {
	private String WEBPAGE = "http://www.google.com";
	
	@Override
	public void run() {
		while (true) {
			synchronized (this) {
				try {
					wait(1);
					List<Proxy> removes = new ArrayList<Proxy>();
					URL url = new URL(WEBPAGE);
					Iterator<Proxy> iterator = ProxyConfig.getProxys()
							.iterator();
					while (iterator.hasNext()) {
						Proxy p = iterator.next();
						try{
							URLConnection conn = url.openConnection(p);
							InputStream in = conn.getInputStream();
						}catch(Exception e){
							e.printStackTrace();
							removes.add(p);
						}
					}
					ProxyConfig.remove(removes);
				} catch (InterruptedException e1) {
					
				} catch (MalformedURLException e) {
					
				}
				
			}
		}
	}
}
