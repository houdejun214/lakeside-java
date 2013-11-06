package com.lakeside.core.utils.time;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lakeside.core.utils.PatternUtils;
import com.lakeside.core.utils.StringUtils;

/**
 * Field trans and make images list
 * 
 * java.text.SimpleDateFormat not thread safe
 * 
 * @author zhufb
 *
 */
public class DateFormat {
	private static final Logger log = LoggerFactory.getLogger("DateFormat");
	
	private static final Pattern DatePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(.*)");
	private static final Pattern DatePattern1 = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
	private static final Pattern DatePattern2 = Pattern.compile("\\w{3} \\w{3} \\d{2} \\d{2}:\\d{2}:\\d{2} \\w{3} \\d{4}");
	private static final Pattern DatePattern3 = Pattern.compile("\\d{4}年\\d{2}月\\d{2}日 ?\\d{2}:\\d{2}");
	private static final Pattern DatePattern4 = Pattern.compile("\\d{4}/\\d{2}/\\d{2}");
	private static final Pattern DatePattern5 = Pattern.compile("\\d{2}-\\d{2}-\\d{4}, \\d{2}:\\d{2} [A|P]M");
	private static final Pattern DatePattern6 = Pattern.compile("\\d{1,2} [A-Za-z]+ \\d{4}");
	private static final Pattern DatePattern7 = Pattern.compile("[A-Za-z]+ \\d{1,2}, \\d{4}");

	public static Object changeStrToDate(Object obj){
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
					dateStr = PatternUtils.replaceMatchGroup("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(.*)", dateStr, 1, "Z");
					SimpleDateFormat Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
					Date date = Format.parse(dateStr);
					return date;
				} catch (Exception e1) {
					log.error("change string to date error:"+dateStr);
					
				}
			}else if(DatePattern1.matcher(dateStr).matches()){
				try {
					SimpleDateFormat Format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date date = Format1.parse(dateStr);
					return date;
				} catch (Exception e1) {
					log.error("change string to date error:"+dateStr);
				}
			}else if(DatePattern2.matcher(dateStr).matches()){
				try {
					SimpleDateFormat Format2 = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",Locale.ENGLISH);
					Date date = Format2.parse(dateStr);
					return date;
				} catch (Exception e1) {
					log.error("change string to date error:"+dateStr);
				}
			}else if(DatePattern3.matcher(dateStr).matches()){
				try {
					SimpleDateFormat Format3 = new SimpleDateFormat("yyyy年MM月dd日HH:mm");
					Date date = Format3.parse(dateStr);
					return date;
				} catch (Exception e1) {
					log.error("change string to date error:"+dateStr);
				}
			}else if(DatePattern4.matcher(dateStr).matches()){
				try {
					SimpleDateFormat Format4 = new SimpleDateFormat("yyyy/MM/dd");
					Date date = Format4.parse(dateStr);
					return date;
				} catch (Exception e1) {
					log.error("change string to date error:"+dateStr);
				}
			}else if(DatePattern5.matcher(dateStr).matches()){
					try {
						SimpleDateFormat Format5 = new SimpleDateFormat("dd-MM-yyyy, hh:mm aa",Locale.ENGLISH);
						Date date = Format5.parse(dateStr);
						return date;
					} catch (Exception e1) {
						log.error("change string to date error:"+dateStr);
					}
			}else if(DatePattern6.matcher(dateStr).matches()){
				try {
					SimpleDateFormat Format6 = new SimpleDateFormat("dd MMMMM yyyy",Locale.ENGLISH);
					Date date = Format6.parse(dateStr);
					return date;
				} catch (Exception e1) {
					log.error("change string to date error:"+dateStr);
				}
			}else if(DatePattern7.matcher(dateStr).matches()){
				try {
					SimpleDateFormat Format7 = new SimpleDateFormat("MMMMM dd, yyyy",Locale.ENGLISH);
					Date date = Format7.parse(dateStr);
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