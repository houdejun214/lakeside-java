package com.lakeside.core.utils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.lakeside.core.utils.domain.DomainSuffixes;


public class UrlUtils {

	private static Pattern IP_PATTERN = Pattern.compile("(\\d{1,3}\\.){3}(\\d{1,3})");
	
	/** Returns the domain name of the url. The domain name of a url is
	   *  the substring of the url's hostname, w/o subdomain names. As an
	   *  example <br><code>
	   *  getDomainName(conf, new URL(http://lucene.apache.org/))
	   *  </code><br>
	   *  will return <br><code> apache.org</code>
	   *   */
	  public static String getDomainName(URL url) {
	    String host = url.getHost();
	    //it seems that java returns hostnames ending with .
	    if(host.endsWith("."))
	      host = host.substring(0, host.length() - 1);
	    if(IP_PATTERN.matcher(host).matches())
	      return host;
	    
	    DomainSuffixes tlds = DomainSuffixes.getInstance();
	    int index = 0;
	    String candidate = host;
	    for(;index >= 0;) {
	      index = candidate.indexOf('.');
	      String subCandidate = candidate.substring(index+1); 
	      if(tlds.isDomainSuffix(subCandidate)) {
	        return candidate; 
	      }
	      candidate = subCandidate;
	    }
	    return candidate;
	  }
	  
	  /** Returns the domain name of the url. The domain name of a url is
	   *  the substring of the url's hostname, w/o subdomain names. As an
	   *  example <br><code>
	   *  getDomainName(conf, new http://lucene.apache.org/)
	   *  </code><br>
	   *  will return <br><code> apache.org</code>
	   * @throws MalformedURLException
	   */
	  public static String getDomainName(String url) throws MalformedURLException {
	    return getDomainName(new URL(url));
	  }
	  
	  
	  /**
	   * Returns the lowercased hostname for the url or null if the url is not well
	   * formed.
	   * 
	   * @param url The url to check.
	   * @return String The hostname for the url.
	   */
	  public static String getHost(String url) {
	    try {
	      return new URL(url).getHost().toLowerCase();
	    }
	    catch (MalformedURLException e) {
	      return null;
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
	 * @param s  an URL query string.
	 * @return a QueryUrl object,compose of a parameter map containing parameter names as keys and parameter values as
	 *         map values, and names list.
	 */
	public static QueryUrl parseQueryUrlString(String s){
		if (s == null)
			return new QueryUrl();
		// In map we use strings and ArrayLists to collect the parameter
		// values.
		HashMap<String, Object> map = new HashMap<String, Object>();
		ArrayList<String> names = new ArrayList<String>();
		int index = s.indexOf("?");
		if(index<0){
			return new QueryUrl();
		}
		String hostName = s.substring(0,index);
		int p = index+1;
		int length = s.length();
		while (p < length) {
			int p0 = p;
			while (p < length && s.charAt(p) != '=' && s.charAt(p) != '&')
				p++;
			//String name = urlDecode(s.substring(p0, p));
			String name = s.substring(p0, p);
			if (p < length && s.charAt(p) == '=')
				p++;
			p0 = p;
			while (p < length && s.charAt(p) != '&')
				p++;
			//String value = urlDecode(s.substring(p0, p));
			String value = s.substring(p0, p);
			if (p < length && s.charAt(p) == '&')
				p++;
			Object x = map.get(name);
			if (x == null) {
				// The first value of each name is added directly as a string to
				// the map.
				map.put(name, value);
				names.add(name);
			} else if (x instanceof String) {
				// For multiple values, we use an ArrayList.
				ArrayList<String> a = new ArrayList<String>();
				a.add((String) x);
				a.add(value);
				map.put(name, a);
			} else {
				ArrayList<String> a = (ArrayList<String>) x;
				a.add(value);
			}
		}
		// Copy map1 to map2. Map2 uses string arrays to store the parameter
		// values.
		
		HashMap<String, String[]> map2 = new HashMap<String, String[]>(map.size());
		for (Map.Entry<String, Object> e : map.entrySet()) {
			String name = e.getKey();
			Object x = e.getValue();
			String[] v;
			if (x instanceof String) {
				v = new String[] { (String) x };
			} else {
				@SuppressWarnings("unchecked")
				ArrayList<String> a = (ArrayList<String>) x;
				v = new String[a.size()];
				v = a.toArray(v);
			}
			map2.put(name, v);
		}
		QueryUrl query = new QueryUrl(hostName,map2,names);
		return query;
	}
	
	public static String urlDecode(String s) {
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Error in urlDecode.", e);
		}
	}
}
