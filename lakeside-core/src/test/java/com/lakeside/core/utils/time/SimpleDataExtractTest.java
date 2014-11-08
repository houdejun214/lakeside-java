package com.lakeside.core.utils.time;

import org.junit.Test;

public class SimpleDataExtractTest {

	@Test
	public void testExtractDate() {
		/**
		 * yyyy-MM-dd'T'HH:mm:ss'Z'
		 * yyyy-MM-dd HH:mm:ss
		 * EEE MMM dd HH:mm:ss zzz yyyy
		 */
		System.out.println(SimpleDateExtract.extractDate("Today 10:52 AM"));
		System.out.println(SimpleDateExtract.extractDate("Today 11:26 AM"));
		System.out.println(SimpleDateExtract.extractDate("2013-12-12T12:12:12"));
		System.out.println(SimpleDateExtract.extractDate("2013-12-12 12:12:12"));
		System.out.println(SimpleDateExtract.extractDate("09-27-2012 12:00 PM"));
		System.out.println(SimpleDateExtract.extractDate("09-27-2012 12:04 PM"));
	}

}
