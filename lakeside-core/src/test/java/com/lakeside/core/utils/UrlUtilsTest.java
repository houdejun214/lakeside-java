package com.lakeside.core.utils;

import java.net.MalformedURLException;

import org.junit.Test;

public class UrlUtilsTest {

	@Test
	public void testParseUrlQueryString() throws MalformedURLException {
		String url = "http://www.google.com.sg/search?aq=f&sourceid=chrome&ie=UTF-8&q=url#sclient=psy-ab&hl=en&source=hp&q=urldsfjkl&pbx=1&oq=urldsfjkl&aq=f&aqi=&aql=&gs_sm=e&gs_upl=1613l1866l0l2262l6l3l0l0l0l0l159l300l2.1l3l0&bav=on.2,or.r_gc.r_pw.r_cp.,cf.osb&fp=b0035702daadcf60&biw=1599&bih=795";
//		 QueryUrl parseQueryUrlString = UrlUtils.parseQueryUrlString(url);
//		System.out.println(parseQueryUrlString);
		
		System.out.println(UrlUtils.getDomainName(url));
	}

}
