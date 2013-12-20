package com.lakeside.download.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.util.EncodingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lakeside.core.utils.PatternUtils;
import com.lakeside.core.utils.StringUtils;
import com.lakeside.download.http.proxy.ProxyConfig;
import com.lakeside.download.http.robots.RobotstxtConfig;
import com.lakeside.download.http.robots.RobotstxtServer;
import com.lakeside.download.http.url.URLCanonicalizer;


/**
 * @author zhufb
 * TODO
 */
public abstract class HttpPageLoader {
	
	private static Logger log = LoggerFactory.getLogger("HttpPageLoader");
	protected static Object syn = new Object();
	protected HttpClient httpClient;
 	protected HttpConfig config;
	protected RobotstxtServer robotstxtServer;
	
	protected HttpPageLoader(){
		this(new HttpConfig(),new RobotstxtConfig());
	}

	protected HttpPageLoader(HttpConfig config,RobotstxtConfig robotstxtConfig){
		this.config = config;
		this.robotstxtServer = new RobotstxtServer(robotstxtConfig,this);
		this.init();
	}
	
	private void init(){
		if (httpClient == null) {
			synchronized (syn) {
				if (httpClient == null) {
					httpClient = getHttpClient();
				}
			}
		}
	}
	
	protected abstract HttpClient getHttpClient();
	
	/**
	 * default simple page loader  
	 * 
	 * @return
	 */
	public static HttpPageLoader getDefaultPageLoader(){
		return new DefaultPageLoader(); 
	}

	/**
	 * advance pageloader with pool connection manager
	 * 
	 * @return
	 */
	public static HttpPageLoader getAdvancePageLoader(){
		return	new AdvancePageLoader(); 
	}


	public HttpPage download(String url){
		return this.download(new HashMap<String, String>(),url);
	}

	public HttpPage download(Map<String, String> header, String url){
		HttpPage page = new HttpPage(url);
		int i = 0;
		while(i < config.getRetryTimes()){
			try {
				return downloadPage(header,url);
			}catch (Exception e) {
				log.error("download web content ["+url +"] failed,{} ",ExceptionUtils.getMessage(e));
				page.setStatusCode(HttpCustomStatus.Excepiton);
			} finally {
				i++;
			}
		}
		return page;
	}
	
//	private void setProxy(String url){
//		HttpHost proxyHost = ProxyConfig.getProxyHost(url);
//		if(proxyHost == null){
//			return;
//		}
//		httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
//	}
	
	private HttpPage downloadPage(Map<String, String> header, String url) throws Exception {
		HttpPage page = new HttpPage(url);
		if(!isRobots(url)&&!robotstxtServer.allows(url)){
			page.setStatusCode(HttpCustomStatus.RobotsNotAllow);
			return page;
		}
		HttpGet get = new HttpGet(url);
		try {
			get.addHeader("Accept-Encoding", "gzip,deflate,sdch");
			get.addHeader("Accept-Charset", "GBK,utf-8");
			if(header!=null&&header.size()>0){
				for(Entry<String, String> entry:header.entrySet()){
					get.addHeader(entry.getKey(), entry.getValue());
				}
			}
//			HttpHost proxyHost = ProxyConfig.getProxyHost(url);
			HttpResponse response = null;
			try{
				response = httpClient.execute(get);
			}catch(NoHttpResponseException e){ 
				HttpGet newGet = new HttpGet(url);
				response = httpClient.execute(newGet);
//				DefaultHttpClient client = getCleanClientTrustingAllSSLCerts();
//				response = client.execute(newGet);
			}catch (IOException e) {
				log.error("Fatal transport error: " + e.getMessage() + " while fetching " + url);
				throw e;
			}
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				if (statusCode != HttpStatus.SC_NOT_FOUND) {
					if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
						Header sheader = response.getFirstHeader("Location");
						if (sheader != null) {
							String redictUrl = sheader.getValue();
							redictUrl = URLCanonicalizer.getCanonicalURL(redictUrl, url);
							return downloadPage(header,redictUrl);
						} 
					}
					log.info("Failed: " + response.getStatusLine().toString() + ", while fetching " + url);
				}
				page.setStatusCode(statusCode);
				return page;
			}

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				long size = entity.getContentLength();
				if (size == -1) {
					Header length = response.getLastHeader("Content-Length");
					if (length == null) {
						length = response.getLastHeader("Content-length");
					}
					if (length != null) {
						size = Integer.parseInt(length.getValue());
					} else {
						size = -1;
					}
				}
				if (size > config.getMaxDownloadSize()) {
					page.setStatusCode(HttpCustomStatus.PageTooBig);
					return page;
				}
				page.load(entity);
				
				this.refinePage(page);
				if(!StringUtils.isEmpty(page.getRedictUrl())){
					return this.downloadPage(header, URLCanonicalizer.getCanonicalURL(page.getRedictUrl(),page.getUrl()));
				}
				page.setStatusCode(HttpStatus.SC_OK);
				return page;
			} else {
				get.abort();
			}
		} catch (Exception e) {
			if (e.getMessage() == null) {
				log.error("Error while fetching " + url);
			} else {
				log.error(e.getMessage() + " while fetching " + url);
			}
			throw e;
		} finally {
			try {
				if (page.getContentData() == null && get != null) {
					get.abort();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		page.setStatusCode(HttpCustomStatus.UnknownError);
		return page;
	}

	private boolean isRobots(String url){
		if(!StringUtils.isEmpty(url)&&url.endsWith("robots.txt")){
			return true;
		}
		return false;
	}
	
	private void refinePage(HttpPage page){
		String content = new String(page.getContentData());
		String redictUrl = PatternUtils.getMatchPattern("HTTP-EQUIV=\"Refresh\".*URL=(.*)\"\\>",
				content, 1);
		if(!StringUtils.isEmpty(redictUrl)){
			page.setRedictUrl(redictUrl);
			return;
		}
		
		if(StringUtils.isEmpty(page.getContentCharset())){
			String charset = PatternUtils.getMatchPattern("(?=<meta).*?(?<=charset=[\\'|\\\"]?)([[a-z]|[A-Z]|[0-9]|-]*)",
						content, 1);
			if(StringUtils.isEmpty(charset)){
				charset = "UTF-8";
			}
			page.setContentCharset(charset);
			page.setContentHtml(EncodingUtils.getString(page.getContentData(), charset));
		}
	}
}
