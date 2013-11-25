
package com.lakeside.download.http.robots;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.lakeside.download.http.HttpPage;
import com.lakeside.download.http.HttpPageLoader;

public class RobotstxtServer {

	protected RobotstxtConfig config;

	protected final Map<String, HostDirectives> host2directivesCache = new HashMap<String, HostDirectives>();
	
	protected HttpPageLoader pageLoader;
	
	public RobotstxtServer(RobotstxtConfig config,HttpPageLoader pageLoader) {
		this.config = config;
		this.pageLoader = pageLoader;
	}

	public boolean allows(String urlstr) {
		if (!config.isEnabled()) {
			return true;
		}
		try {
			URL url = new URL(urlstr);
			String host = url.getHost().toLowerCase();
			String path = url.getPath();

			HostDirectives directives = host2directivesCache.get(host);

            if (directives != null && directives.needsRefetch()) {
                synchronized (host2directivesCache) {
                    host2directivesCache.remove(host);
                    directives = null;
                }
            }

			if (directives == null) {
				directives = fetchDirectives(host);
			}
			return directives.allows(path);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return true;
	}

	private HostDirectives fetchDirectives(String host) {
		String robotsTxtUrl = "http://" + host + "/robots.txt";
		HostDirectives directives = null;
		HttpPage page = pageLoader.download(robotsTxtUrl);
		directives = RobotstxtParser.parse(page.getContentHtml(), config.getUserAgentName());
		if (directives == null) {
			// We still need to have this object to keep track of the time we
			// fetched it
			directives = new HostDirectives();
		}
		synchronized (host2directivesCache) {
			if (host2directivesCache.size() == config.getCacheSize()) {
				String minHost = null;
				long minAccessTime = Long.MAX_VALUE;
				for (Entry<String, HostDirectives> entry : host2directivesCache.entrySet()) {
					if (entry.getValue().getLastAccessTime() < minAccessTime) {
						minAccessTime = entry.getValue().getLastAccessTime();
						minHost = entry.getKey();
					}
				}
				host2directivesCache.remove(minHost);
			}
			host2directivesCache.put(host, directives);
		}
		return directives;
	}

}
