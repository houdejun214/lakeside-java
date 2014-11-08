package com.lakeside.core.utils.time;

import com.lakeside.core.utils.PatternUtils;
import com.lakeside.core.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * DateFormat for str to Date with DatePattern support
 * 
 * java.text.SimpleDateFormat not thread safe
 * 
 * @author zhufb
 * 
 */
public class DateFormat {
	
	private static final Logger log = LoggerFactory.getLogger("DateFormat");

	public static Date strToDate(Object obj) {
		if (obj == null) {
			return null;
		}
		if(obj instanceof Date){
			return (Date)obj;
		}
		String dateStr = StringUtils.valueOf(obj);
		if (StringUtils.isEmpty(dateStr)) {
			return null;
		}
		try {
			Date date = new Date(dateStr);
			return date;
		} catch (Exception e) {
			for (DatePattern p : DatePattern.values()) {
				try {
					if (p.getPattern().matcher(dateStr).matches()) {
						if(p.equals(DatePattern.Pattern_1)){
							dateStr = PatternUtils.replaceMatchGroup(DatePattern.Pattern_1.getPatternStr(), dateStr, 1,"Z");
						}
						SimpleDateFormat Format = new SimpleDateFormat(p.getFormat(), Locale.ENGLISH);
						return Format.parse(dateStr);
					}
				} catch (Exception e1) {
					log.error("change string to date error:" + dateStr);

				}
			}
			if (StringUtils.isNum(dateStr)) {
				Date date = DateTimeUtils.getTimeFromUnixTime(dateStr);
				return date;
			}
			log.warn("change string to date faild [" + dateStr + "]");
		}
		return null;
	}
	
	public static Date findDateInStr(String str) {
		if(StringUtils.isEmpty(str)){
			return null;
		}
		for (DatePattern p : DatePattern.values()) {
			Matcher matcher = p.getPattern().matcher(str);
			if (matcher.find()) {
				String strDate = matcher.group(0);
				return strToDate(strDate);
			}
		}
		return null;
	}

	public static Date findFirstDateInStr(String str) {
		if(StringUtils.isEmpty(str)){
			return null;
		}
		int start = Integer.MAX_VALUE;
		Date result = null;
		for (DatePattern p : DatePattern.values()) {
			Matcher matcher = p.getPattern().matcher(str);
			if (matcher.find()) {
				String strDate = matcher.group(0);
				Date date = DateFormat.strToDate(strDate);
				if(date!=null&&matcher.start()<start){
					result =date;
					start = matcher.start();
				}
			}
		}
		return result;
	}
}