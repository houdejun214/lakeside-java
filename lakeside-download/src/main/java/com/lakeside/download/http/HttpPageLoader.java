package com.lakeside.download.http;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParamBean;
import org.apache.http.protocol.HttpContext;
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
public class HttpPageLoader {
	
	private static Logger log = LoggerFactory.getLogger("SdataCrawler.HttpPageLoader");
	private HttpConfig config;
	private RobotstxtServer robotstxtServer;
	private HttpClient httpClient;
	private static final ThreadLocal<HttpClient> userThreadLocal = new ThreadLocal<HttpClient>();
	private HttpClient getClient(){
		DefaultHttpClient client = (DefaultHttpClient)userThreadLocal.get();
		if(client == null){
			try{
				HttpParams params = new BasicHttpParams();
				HttpProtocolParamBean paramsBean = new HttpProtocolParamBean(params);
				paramsBean.setVersion(HttpVersion.HTTP_1_1);
				paramsBean.setContentCharset("UTF-8");
				paramsBean.setUseExpectContinue(false);
				params.setParameter(CoreProtocolPNames.USER_AGENT, config.getUserAgentString());
				params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, config.getSocketTimeout());
				params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, config.getConnectionTimeout());
				params.setBooleanParameter("http.protocol.handle-redirects", false);
				params.setBooleanParameter("http.protocol.single-cookie-header", true);
	
				SchemeRegistry schemeRegistry = new SchemeRegistry();
				schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
				if (config.isIncludeHttpsPages()) {
					 SSLContext sc = SSLContext.getInstance("SSL");
			         sc.init(null, getTrustingManager(), new java.security.SecureRandom());
			         SSLSocketFactory socketFactory = new SSLSocketFactory(sc);
					 schemeRegistry.register(new Scheme("https", 443, socketFactory));
				}
				PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(schemeRegistry);
				connectionManager.setMaxTotal(config.getMaxTotalConnections());
				connectionManager.setDefaultMaxPerRoute(config.getMaxConnectionsPerHost());
				client = new DefaultHttpClient(connectionManager, params);
				client.addResponseInterceptor(new HttpResponseInterceptor() {
			            public void process(final HttpResponse response, final HttpContext context) throws HttpException,
			                    IOException {
			                HttpEntity entity = response.getEntity();
			                Header contentEncoding = entity.getContentEncoding();
			                if (contentEncoding != null) {
			                    HeaderElement[] codecs = contentEncoding.getElements();
			                    for (HeaderElement codec : codecs) {
			                        if (codec.getName().equalsIgnoreCase("gzip")) {
			                            response.setEntity(new GzipDecompressingEntity(response.getEntity()));
			                            return;
			                        }
			                    }
			                }
			            }
	
			        });
	
				userThreadLocal.set(client);
			}catch(Exception e){
				e.printStackTrace();
				throw new RuntimeException("Init http client failed ",e);
			}
		}
		return client;
	}
	public static HttpPageLoader getDefaultPageLoader(){
		return new HttpPageLoader(new HttpConfig(),new RobotstxtConfig());
	}

	public HttpPageLoader(HttpConfig config){
		new HttpPageLoader(config,new RobotstxtConfig());
	}
	
	public HttpPageLoader(HttpConfig config,RobotstxtConfig robotstxtConfig){
		this.config = config;
		this.httpClient = getClient();
		this.robotstxtServer = new RobotstxtServer(robotstxtConfig,this);
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
	
	private void setProxy(String url){
		HttpHost proxyHost = ProxyConfig.getProxyHost(url);
		if(proxyHost == null){
			return;
		}
		httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
	}
	
	private HttpPage downloadPage(Map<String, String> header, String url) throws Exception {
		HttpPage page = new HttpPage(url);
		if(!isRobots(url)&&!robotstxtServer.allows(url)){
			page.setStatusCode(HttpCustomStatus.RobotsNotAllow);
			return page;
		}
		HttpGet get = new HttpGet(url);
		try {
			get.addHeader("Accept-Encoding", "gzip");
			get.addHeader("Accept-Charset", "GBK,utf-8");
			if(header!=null&&header.size()>0){
				for(Entry<String, String> entry:header.entrySet()){
					get.addHeader(entry.getKey(), entry.getValue());
				}
			}
			// Set proxy host if need
			this.setProxy(url);
			HttpResponse response = null;
			try{
				response = httpClient.execute(get);
			}catch(NoHttpResponseException e){ 
				HttpGet newGet = new HttpGet(url);
				DefaultHttpClient client = getCleanClientTrustingAllSSLCerts();
				response = client.execute(newGet);
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

    private TrustManager[] getTrustingManager() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                // Do nothing
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                // Do nothing
            }

        } };
        return trustAllCerts;
    }
    
    private DefaultHttpClient getCleanClientTrustingAllSSLCerts() throws NoSuchAlgorithmException, KeyManagementException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, getTrustingManager(), new java.security.SecureRandom());
        SSLSocketFactory socketFactory = new SSLSocketFactory(sc);
        Scheme sch = new Scheme("https", 443, socketFactory);
        httpclient.getConnectionManager().getSchemeRegistry().register(sch);
        return httpclient;
    }
}
