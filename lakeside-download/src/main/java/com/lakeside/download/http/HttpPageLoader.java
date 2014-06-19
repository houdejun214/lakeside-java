package com.lakeside.download.http;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EncodingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lakeside.core.utils.PatternUtils;
import com.lakeside.core.utils.StringUtils;
import com.lakeside.core.utils.UrlUtils;
import com.lakeside.download.http.robots.RobotstxtConfig;
import com.lakeside.download.http.robots.RobotstxtServer;
import com.lakeside.download.http.url.URLCanonicalizer;


/**
 * @author zhufb
 * TODO
 */
public abstract class HttpPageLoader {

    public static final HashMap<String, String> EMPTY = new HashMap<String, String>();
    private static Logger log = LoggerFactory.getLogger("HttpPageLoader");
    protected static Object syn = new Object();
    protected HttpClient httpClient;
    protected HttpConfig config;
    protected RobotstxtServer robotstxtServer;


    protected HttpPageLoader(){
        this(new HttpConfig(), new RobotstxtConfig());
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

    /**
     * set the HTTPConfig
     * @param config
     * @return
     */
    public HttpPageLoader withHttpConfig(HttpConfig config){
        this.config = config;
        return this;
    }

    /**
     * post a http get request
     * @param url
     * @return
     */
    public HttpPage get(String url){
        return this.get(EMPTY, url);
    }

    /**
     * post a http get request
     * @param header
     * @param url
     * @return
     */
    public HttpPage get(Map<String, String> header, String url){
        HttpPage page = new HttpPage(url);
        int i = 0;
        while(i < config.getRetryTimes()){
            try {
                return downloadPage(header, null, url, HttpRequestType.GET);
            }catch (Exception e) {
                log.error("get web content ["+url +"] failed,{} ",ExceptionUtils.getMessage(e));
                page.setStatusCode(HttpCustomStatus.Excepiton);
            } finally {
                i++;
            }
        }
        return page;
    }

    /**
     * send a http post request
     * @param data
     * @param url
     * @return
     */
    public HttpPage post(Map<String, Object> data, String url){
        return this.post(EMPTY, data, url);
    }

    /**
     * send a http post request
     * @param header
     * @param data
     * @param url
     * @return
     */
    public HttpPage post(Map<String, String> header, Map<String, Object> data, String url){
        HttpPage page = new HttpPage(url);
        int i = 0;
        while(i < config.getRetryTimes()){
            try {
                return downloadPage(header, data, url, HttpRequestType.POST);
            }catch (Exception e) {
                log.error("get web content ["+url +"] failed,{} ",ExceptionUtils.getMessage(e));
                page.setStatusCode(HttpCustomStatus.Excepiton);
            } finally {
                i++;
            }
        }
        return page;
    }

    /**
     * perform the real download operation
     * @param header
     * @param data
     * @param url
     * @param type
     * @return
     * @throws Exception
     */
    private HttpPage downloadPage(Map<String, String> header, Map<String, Object> data, String url, HttpRequestType type) throws Exception {
        HttpPage page = new HttpPage(url);
        if(!isRobots(url)&&!robotstxtServer.allows(url)){
            page.setStatusCode(HttpCustomStatus.RobotsNotAllow);
            return page;
        }
        HttpRequestBase request = getRequest(url,type);
        try {
            request.addHeader("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.1; pl; rv:1.9.1) Gecko/20090624 Firefox/3.5 (.NET CLR 3.5.30729)");
            request.addHeader("Accept-Encoding", "gzip,deflate,sdch");
            request.addHeader("Accept-Charset", "GBK,utf-8");
            request.addHeader("Connection", "close");
            if(header!=null&&header.size()>0){
                for(Entry<String, String> entry:header.entrySet()){
                    request.addHeader(entry.getKey(), entry.getValue());
                }
            }
            if(HttpRequestType.POST.equals(type)){
                setPostData(data, request);
            }
            HttpResponse response = null;
            try{
                response = httpClient.execute(request);
            }catch(NoHttpResponseException e){
                HttpGet newGet = new HttpGet(url);
                response = httpClient.execute(newGet);
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
                            return downloadPage(header,data, redictUrl,type);
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
                    String canonicalURL = URLCanonicalizer.getCanonicalURL(page.getRedictUrl(), page.getUrl());
                    return this.downloadPage(header, data, canonicalURL,type );
                }
                page.setStatusCode(HttpStatus.SC_OK);
                return page;
            } else {
                request.abort();
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
                if (page.getContentData() == null && request != null) {
                    request.abort();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        page.setStatusCode(HttpCustomStatus.UnknownError);
        return page;
    }

    /**
     * set post data to the request
     * @param data
     * @param request
     * @throws IOException
     */
    private void setPostData(Map<String, Object> data, HttpRequestBase request) throws IOException {
        if(request!=null && request instanceof HttpPost) {
            HttpPost post = (HttpPost) request;
            // append parameters for signature security

            if (data != null) {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                for (Entry<String, Object> entry : data.entrySet()) {
                    if (entry.getValue() != null && entry.getValue() instanceof File) {
                        // post image file to spring, make sure the image name is not empty, otherwise the springmvc can't recognition the upload image
                        //TODO : upload file need to use MultipartEntity
                    } else {
                        nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
                    }
                }
                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            }
        }
    }

    private boolean isRobots(String url){
        if(!StringUtils.isEmpty(url)&&url.endsWith("robots.txt")){
            return true;
        }
        return false;
    }

    private void refinePage(HttpPage page){
        String content = new String(page.getContentData());
        if(content!=null&&!content.contains("body")){
            String redictUrl = PatternUtils.getMatchPattern("http-equiv=\"refresh\" [a-z]*url=(.*)\"\\/?>",content, 1,Pattern.CASE_INSENSITIVE);
            if(!StringUtils.isEmpty(redictUrl)){
                redictUrl = UrlUtils.decode(redictUrl);
                redictUrl=StringEscapeUtils.unescapeXml(redictUrl);
                redictUrl= redictUrl.replaceAll("['\"]", "");
                page.setRedictUrl(redictUrl);
                return;
            }
        }
        if(StringUtils.isEmpty(page.getContentCharset())){
            String charset = PatternUtils.getMatchPattern("(?=<meta).*?(?<=charset=['\"]?)([a-zA-Z0-9|-]+)",content, 1,Pattern.CASE_INSENSITIVE);
            if(StringUtils.isEmpty(charset)){
                charset = "UTF-8";
            }
            page.setContentCharset(charset);
            page.setContentHtml(EncodingUtils.getString(page.getContentData(), charset));
        }
    }

    private HttpRequestBase getRequest(String url,HttpRequestType type){
        switch (type) {
            case POST:
                return new HttpPost(url);
            case GET:
            default:
                return new HttpGet(url);
        }
    }

    static enum HttpRequestType {
        POST,
        GET,
        DELETE
    }
}
