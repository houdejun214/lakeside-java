package com.lakeside.core.utils.time;

import java.util.regex.Pattern;

/**
 * DatePattern for support date str format
 * 
 * 
 * @author zhufb
 *
 */
public enum DatePattern {
	
	Pattern_1("yyyy-MM-dd'T'HH:mm:ss'Z'","\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(.*)"),
	Pattern_2("yyyy-MM-dd HH:mm:ss","\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
	Pattern_3("EEE MMM dd HH:mm:ss zzz yyyy","\\w{3} \\w{3} \\d{2} \\d{2}:\\d{2}:\\d{2} \\w{3} \\d{4}"),
	Pattern_4("EEE MMM dd HH:mm:ss zzz yyyy","\\w{3} \\d{2} \\d{2}:\\d{2}:\\d{2} \\d{4}"),
	Pattern_5("yyyy-MM-dd HH:mm","\\d{4}-\\d{2}-\\d{2} ?\\d{2}:\\d{2}"),
	Pattern_6("yyyy-MM-dd","\\d{4}-\\d{2}-\\d{2}"),
	Pattern_7("yyyy年MM月dd日HH:mm","\\d{4}年\\d{2}月\\d{2}日 ?\\d{2}:\\d{2}"),
	Pattern_8("yyyy年MM月dd日","\\d{4}年\\d{2}月\\d{2}日"),
	Pattern_9("yyyy/MM/dd","\\d{4}/\\d{2}/\\d{2}"),
	Pattern_10("dd-MM-yyyy,hh:mm aa","\\d{2}-\\d{2}-\\d{4}, ?\\d{2}:\\d{2} [A|P]M"),
	Pattern_11("dd MMMMM yyyy","\\d{1,2} [A-Za-z]+ \\d{4}"),
	Pattern_12("MMMMM dd,yyyy HH:mm","[A-Za-z]{3} \\d{1,2}, ?\\d{4} \\d{2}:\\d{2}"),
	Pattern_13("MMMMM dd,yyyy","[A-Za-z]{5} \\d{1,2}, ?\\d{4}"),
	Pattern_14("MMMMM dd,yyyy","[A-Za-z]{3} \\d{1,2}, ?\\d{4}");
	
	private String format;
	private String patternStr;
	private Pattern pattern;
	
	DatePattern(String format,String patternStr){
		this.format = format; 
		this.patternStr = patternStr;
		this.pattern = Pattern.compile(patternStr);
	}

	public String getFormat() {
		return format;
	}

	public String getPatternStr() {
		return patternStr;
	}

	public Pattern getPattern() {
		return pattern;
	}
	
}