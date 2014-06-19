
package com.lakeside.download.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;

/**
 * @author zhufb
 *
 */
public class HttpPage {

    protected int statusCode;

    /**
     * The URL of this page.
     */
    protected String url;

    /**
     * The redict URL of this page.
     */
    protected String redictUrl;

    /**
     * The content of this page in binary format.
     */
    protected byte[] contentData;

    /**
     * The ContentType of this page.
     * For example: "text/html; charset=UTF-8"
     */
    protected String contentType;

    /**
     * The encoding of the content.
     * For example: "gzip"
     */
    protected String contentEncoding;

    /**
     * The charset of the content.
     * For example: "UTF-8"
     */
    protected String contentCharset;

    /**
     * The conte of this page in html
     */
    protected String contentHtml;


    public HttpPage(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }


    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Loads the content of this page from a fetched
     * HttpEntity.
     */
    public void load(HttpEntity entity) throws Exception {

        contentType = null;
        Header type = entity.getContentType();
        if (type != null) {
            contentType = type.getValue();
        }

        contentEncoding = null;
        Header encoding = entity.getContentEncoding();
        if (encoding != null) {
            contentEncoding = encoding.getValue();
        }

        ContentType ct = ContentType.get(entity);
        if(ct != null&&ct.getCharset()!=null){
            contentCharset = ct.getCharset().toString();
        }

        contentData = EntityUtils.toByteArray(entity);

        if(contentCharset!=null && contentData!=null){
            contentHtml = EncodingUtils.getString(contentData, contentCharset);
        }
    }

    /**
     * Returns the content of this page in binary format.
     */
    public byte[] getContentData() {
        return contentData;
    }

    public void setContentData(byte[] contentData) {
        this.contentData = contentData;
    }

    /**
     * Returns the ContentType of this page.
     * For example: "text/html; charset=UTF-8"
     */
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Returns the encoding of the content.
     * For example: "gzip"
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    /**
     * Returns the charset of the content.
     * For example: "UTF-8"
     */
    public String getContentCharset() {
        return contentCharset;
    }

    public void setContentCharset(String contentCharset) {
        this.contentCharset = contentCharset;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getContentHtml() {
        return contentHtml;
    }

    public String getContent() { return contentHtml; }

    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }

    public String getRedictUrl() {
        return redictUrl;
    }

    public void setRedictUrl(String redictUrl) {
        this.redictUrl = redictUrl;
    }
}
