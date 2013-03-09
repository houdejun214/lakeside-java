package com.lakeside.core.utils.time;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lakeside.core.utils.StringUtils;

/**
 * Field trans and make images list
 * 
 * 
 * @author zhufb
 *
 */
public class DateFormat {

	private static final Logger log = LoggerFactory.getLogger("DateFormat");
	private static final Pattern DatePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z");
	private static final Pattern DatePattern1 = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
	private static final Pattern DatePattern2 = Pattern.compile("\\w{3} \\w{3} \\d{2} \\d{2}:\\d{2}:\\d{2} \\w{3} \\d{4}");
	public static Object changeStrToDate(Object obj){
		SimpleDateFormat Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		SimpleDateFormat Format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat Format2 = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",Locale.ENGLISH);
		if(obj==null || obj instanceof Date){
			return obj;
		}
		String dateStr = StringUtils.valueOf(obj);
		if(StringUtils.isEmpty(dateStr)) {
			return null;
		}
		try {
			Date date = new Date(dateStr);
			return date;
		} catch (Exception e) {
			if(DatePattern.matcher(dateStr).matches()){
				try {
					Date date = Format.parse(dateStr);
					return date;
				} catch (Exception e1) {
					log.error("change string to date error:"+dateStr);
					
				}
			}else if(DatePattern1.matcher(dateStr).matches()){
				try {
					Date date = Format1.parse(dateStr);
					return date;
				} catch (Exception e1) {
					log.error("change string to date error:"+dateStr);
				}
			}else if(DatePattern2.matcher(dateStr).matches()){
				try {
					Date date = Format2.parse(dateStr);
					return date;
				} catch (Exception e1) {
					log.error("change string to date error:"+dateStr);
				}
			}else if(StringUtils.isNum(dateStr)){
				Date date = DateTimeUtils.getTimeFromUnixTime(dateStr);
				return date;
			}
			log.warn("change string to date faild ["+dateStr+"]");
		}
		return null;
	}
}