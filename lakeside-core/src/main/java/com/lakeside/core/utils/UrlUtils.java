package com.lakeside.core.utils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Pattern;

import com.lakeside.core.utils.domain.DomainSuffixes;

public class UrlUtils {

	private static Pattern IP_PATTERN = Pattern
			.compile("(\\d{1,3}\\.){3}(\\d{1,3})");

	/**
	 * Returns the domain name of the url. The domain name of a url is the
	 * substring of the url's hostname, w/o subdomain names. As an example <br>
	 * <code>
	 *  getDomainName(conf, new URL(http://lucene.apache.org/))
	 *  </code><br>
	 * will return <br>
	 * <code> apache.org</code>
	 * */
	public static String getDomainName(URL url) {
		String host = url.getHost();
		// it seems that java returns hostnames ending with .
		if (host.endsWith("."))
			host = host.substring(0, host.length() - 1);
		if (IP_PATTERN.matcher(host).matches())
			return host;

		DomainSuffixes tlds = DomainSuffixes.getInstance();
		int index = 0;
		String candidate = host;
		for (; index >= 0;) {
			index = candidate.indexOf('.');
			String subCandidate = candidate.substring(index + 1);
			if (tlds.isDomainSuffix(subCandidate)) {
				return candidate;
			}
			candidate = subCandidate;
		}
		return candidate;
	}

	/**
	 * Returns the domain name of the url. The domain name of a url is the
	 * substring of the url's hostname, w/o subdomain names. As an example <br>
	 * <code>
	 *  getDomainName(conf, new http://lucene.apache.org/)
	 *  </code><br>
	 * will return <br>
	 * <code> apache.org</code>
	 * 
	 * @throws MalformedURLException
	 */
	public static String getDomainName(String url) throws MalformedURLException {
		return getDomainName(new URL(url));
	}

	/**
	 * Returns the lowercased hostname for the url or null if the url is not
	 * well formed.
	 * 
	 * get full host name http://163.com/** return 163.com
	 * http://news.163.com/** return news.163.com
	 * 
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 */
	public static String getHost(String url) {
		try {
			String host = new URL(url).getHost().toLowerCase();
			int index = host.indexOf("/");
			if (index > 0) {
				host = host.substring(0, index);
			}
			if(host.toLowerCase().startsWith("www.")){
				host = host.substring(4);
			}
			return host.toLowerCase();
		} catch (MalformedURLException e) {
			return "";
		}
	}

	/**
	 * Parses an URL query string and returns a map with the parameter values.
	 * The URL query string is the part in the URL after the first '?' character
	 * up to an optional '#' character. It has the format
	 * "name=value&name=value&...". The map has the same structure as the one
	 * returned by javax.servlet.ServletRequest.getParameterMap(). A parameter
	 * name may occur multiple times within the query string. For each parameter
	 * name, the map contains a string array with the parameter values.
	 * 
	 * @param s
	 *            an URL query string.
	 * @return a QueryUrl object,compose of a parameter map containing parameter
	 *         names as keys and parameter values as map values, and names list.
	 */
	public static QueryUrl parseQueryUrlString(String s) {
		return new QueryUrl(s);
	}

	public static String decode(String s) {
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Error in urlDecode.", e);
		}
	}

	public static String clean(String url) {
		url = StringUtils.trim(url);
		if (StringUtils.isEmpty(url)) {
			return null;
		}
		url = url.replaceAll("\\\\", "");
		url = url.replaceAll("\"", "");
		url = url.replaceAll("\'", "");
		int index = url.indexOf("#");
		if(index > 0){
			url = url.substring(0,index);
		}
		return url;
	}

	public static String encode(String s) {
		return encode(s, "UTF-8");
	}

	public static String encode(String s, String charset) {
		try {
			return URLEncoder.encode(s, charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Error in urlDecode.", e);
		}
	}
}
