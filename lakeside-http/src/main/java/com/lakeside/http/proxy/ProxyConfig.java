package com.lakeside.http.proxy;

import com.lakeside.core.utils.ApplicationResourceUtils;
import com.lakeside.core.utils.StringUtils;
import org.apache.http.HttpHost;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * TODO 需要优化，
 * 1、需按照 key:regx 如 http://sina.com/*的url，value 代理的ip地址和端口列表。
 * 
 * 2、程序可以检测哪些代理不可用了，从而放弃使用 ProxyMonitor
 *
 */
public class ProxyConfig {

	private static ProxyMonitor monitor = new ProxyMonitor();
	/**
	 * curProxy index
	 */
	private static int curProxy = 0;
	
	/**
	 * proxy list
	 */
	private static List<Proxy> proxys = new ArrayList<Proxy>();
	
	public static  Proxy getProxy(){
		if(proxys == null || proxys.size()<=0){
			return null;
		}
		synchronized (proxys) {
			curProxy = (curProxy+1)% proxys.size();
			Proxy proxy = proxys.get(curProxy);
			return proxy;
		}
	}
	
	/**
	 * 
	 * TODO 根据URL确定是否需要使用代理，如果使用代理是使用哪些代理 
	 * @param url
	 * @return
	 */
	public static HttpHost getProxyHost(String url){
		Proxy proxy = getProxy();
		if(proxy==null){
			return null;
		}
		InetSocketAddress addr = (InetSocketAddress)proxy.address();
		return new HttpHost(addr.getHostName(),addr.getPort());
	}
	
	public static void loadAndSetProxy(String path) {
		if(StringUtils.isEmpty(path)){
			return;
		}
		try {
			BufferedReader reader = null;
			String absPath = ApplicationResourceUtils.getResourceUrl(path);
			File file = new File(absPath);
			if(file.exists()){
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			}else{
				InputStream stream = ApplicationResourceUtils.getResourceStream(path);
				reader = new BufferedReader(new InputStreamReader(stream));
			}
			String line;
			while((line = reader.readLine())!=null){
				String[] splits = line.trim().split(":");
				if (splits != null && splits.length == 2) {
					String ip = splits[0];
					Integer port = StringUtils.toInt(splits[1]);
					InetSocketAddress addr = new InetSocketAddress(ip, port);
					Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
					addProxy(proxy);
				}
			}
		} catch (IOException e) {
			
		}
		monitor.start();
	}

	public static List<Proxy> getProxys() {
		return proxys;
	}
	
	public static void removeProxy(Proxy proxy) {
		synchronized (proxys) {
			proxys.remove(proxy);
		}
	}

	public static void remove(List<Proxy> proxys) {
		synchronized (proxys) {
			proxys.removeAll(proxys);
		}
	}

	public static void addProxy(Proxy proxy) {
		synchronized (proxys) {
			proxys.add(proxy);
		}
	}
	
	
}
