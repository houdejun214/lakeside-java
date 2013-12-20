package com.lakeside.core.utils.time;

import org.junit.Test;

public class DataFormatTest {

//	Pattern_1("yyyy-MM-dd'T'HH:mm:ss'Z'","\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(.*)"),
//	Pattern_2("yyyy-MM-dd HH:mm:ss","\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
//	Pattern_3("EEE MMM dd HH:mm:ss zzz yyyy","\\w{3} \\w{3} \\d{2} \\d{2}:\\d{2}:\\d{2} \\w{3} \\d{4}"),
//	Pattern_4("yyyy年MM月dd日HH:mm","\\d{4}年\\d{2}月\\d{2}日 ?\\d{2}:\\d{2}"),
//	Pattern_5("yyyy/MM/dd","\\d{4}/\\d{2}/\\d{2}"),
//	Pattern_6("dd-MM-yyyy, hh:mm aa","\\d{2}-\\d{2}-\\d{4}, \\d{2}:\\d{2} [A|P]M"),
//	Pattern_7("dd MMMMM yyyy","\\d{1,2} [A-Za-z]+ \\d{4}"),
//	Pattern_8("MMMMM dd, yyyy","[A-Za-z]+ \\d{1,2}, \\d{4}");
	
	@Test
	public void test() {
		/**
		 * yyyy-MM-dd'T'HH:mm:ss'Z'
		 * yyyy-MM-dd HH:mm:ss
		 * EEE MMM dd HH:mm:ss zzz yyyy
		 */
		
		// strToDate
//		System.out.println(DateFormat.strToDate("2013-12-12T12:12:12.s"));
//		System.out.println(DateFormat.strToDate("2013-12-12T12:12:12Z"));
//		System.out.println(DateFormat.strToDate("2013-12-12 12:12:12"));
//		System.out.println(DateFormat.strToDate("Dec 12 12:12:12 2013"));
		System.out.println(DateFormat.strToDate("2013年12月12日12:12"));
		System.out.println(DateFormat.strToDate("2013年12月12日 12:12"));
//		System.out.println(DateFormat.strToDate("2013/12/12"));
//		System.out.println(DateFormat.strToDate("12-12-2013, 12:12 PM"));
//		System.out.println(DateFormat.strToDate("12-12-2013,12:12 PM"));
//		System.out.println(DateFormat.strToDate("12 Dec 2013"));
//		System.out.println(DateFormat.strToDate("Dec 12, 2013"));
		System.out.println(DateFormat.strToDate("2013年12月20日 08:51"));
		System.out.println(DateFormat.strToDate("2013年12月20日"));
		System.out.println(DateFormat.strToDate("2013-12-20 03:09"));
		
		// findDate

		System.out.println(DateFormat.findDateInStr("sdfdfdffd 2013-12-12T12:12:12.sdfdfdfdfd"));
		System.out.println(DateFormat.findDateInStr("sdfdfdffd Dec 12,2013dfdfdfdfd"));
		System.out.println(DateFormat.findDateInStr("sdfdfdffd 2013-12-12T12:12:12Zdfdfdfd"));
		System.out.println(DateFormat.findDateInStr("sdfdfdffDec 12 12:12:12 2013dfdfdfdfd"));
		System.out.println(DateFormat.findDateInStr("sdfdfdff2013年12月12日12:12dfdfdfd"));
		System.out.println(DateFormat.findDateInStr("sdfdfdff2013年12月12日 12:12fdfdfd"));
		System.out.println(DateFormat.findDateInStr("sdfdfdf2013/12/12dfdfd"));
		System.out.println(DateFormat.findDateInStr("sdfdfdff2013年12月12日 12:12fdfdfd"));
		System.out.println(DateFormat.findDateInStr("sdfdfdff12-12-2013,12:12 PMdfd"));
		System.out.println(DateFormat.findDateInStr("sdfdfd12-12-2013,12:12 PMfdfd"));
		System.out.println(DateFormat.findDateInStr("sdfdfd12 Dec 2013dfd"));
		System.out.println(DateFormat.findDateInStr("sdfdfDec 12, 2013fd"));
		System.out.println(DateFormat.findDateInStr("sdfDec 12,2013"));
	}

}
