package com.lakeside.core.utils.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从不规则的文本中抽取时间类型
 * 
 * @author houdejun
 *
 */
@Deprecated
public class SimpleDateExtract {

	private static final String REGEX_DELIMITER = "[-:\\/.,]";
	private static final String REGEX_DAY = "((?:[0-2]?\\d{1})|(?:[3][01]{1}))";
	private static final String REGEX_MONTH = "(?:([0]?[1-9]|[1][012])|(Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Sept|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?))";
	private static final String REGEX_YEAR = "((?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3}))";
	private static final String REGEX_HOUR_MINUTE_SECOND = "(?:(?:[\\sT\\W]{0,2})((?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):([0-5][0-9])(?::([0-5][0-9]))?(?:\\s?(am|AM|pm|PM))?)?";
	private static final String REGEX_ENDSWITH = "(?![\\d])";
	// DD/MM/YYYY
	private static final String REGEX_DATE_EUROPEAN = REGEX_DAY + REGEX_DELIMITER + REGEX_MONTH + REGEX_DELIMITER + REGEX_YEAR +REGEX_HOUR_MINUTE_SECOND + REGEX_ENDSWITH;
	// MM/DD/YYYY
	private static final String REGEX_DATE_AMERICAN = REGEX_MONTH + REGEX_DELIMITER + REGEX_DAY + REGEX_DELIMITER + REGEX_YEAR +REGEX_HOUR_MINUTE_SECOND + REGEX_ENDSWITH;
	// YYYY/MM/DD
	private static final String REGEX_DATE_TECHNICAL = REGEX_YEAR + REGEX_DELIMITER + REGEX_MONTH + REGEX_DELIMITER + REGEX_DAY +REGEX_HOUR_MINUTE_SECOND + REGEX_ENDSWITH;

	private static final SimpleDateFormat DayFormat = new SimpleDateFormat("yyyy-MM-dd");
	/**
	 * Extract date
	 * 
	 * @return Date object
	 * @throws ParseException 
	 */
	public static Date extractDate(String text) {
		String today = DayFormat.format(new Date());
		
		text = text.replaceAll("t|Today", today);
		
	    Date date = null;
	    boolean dateFound = false;

	    String year = null;
	    String month = null;
	    String monthName = null;
	    String day = null;
	    String hour = null;
	    String minute = null;
	    String second = null;
	    String ampm = null;

	    
	    
	    // see if there are any matches
	    Matcher m = checkDatePattern(REGEX_DATE_EUROPEAN, text);
	    if (m.find()) {
	        day = m.group(1);
	        month = m.group(2);
	        monthName = m.group(3);
	        year = m.group(4);
	        hour = m.group(5);
	        minute = m.group(6);
	        second = m.group(7);
	        ampm = m.group(8);
	        dateFound = true;
	    }

	    if(!dateFound) {
	        m = checkDatePattern(REGEX_DATE_AMERICAN, text);
	        if (m.find()) {
	            month = m.group(1);
	            monthName = m.group(2);
	            day = m.group(3);
	            year = m.group(4);
	            hour = m.group(5);
	            minute = m.group(6);
	            second = m.group(7);
	            ampm = m.group(8);
	            dateFound = true;
	        }
	    }

	    if(!dateFound) {
	        m = checkDatePattern(REGEX_DATE_TECHNICAL, text);
	        if (m.find()) {
	            year = m.group(1);
	            month = m.group(2);
	            monthName = m.group(3);
	            day = m.group(4);
	            hour = m.group(5);
	            minute = m.group(6);
	            second = m.group(7);
	            ampm = m.group(8);
	            dateFound = true;
	        }
	    }

	    // construct date object if date was found
	    if(dateFound) {
	        String dateFormatPattern = "";
	        String dayPattern = "";
	        String dateString = "";

	        if(day != null) {
	            dayPattern = "d" + (day.length() == 2 ? "d" : "");
	        }

	        if(day != null && month != null && year != null) {
	            dateFormatPattern = "yyyy MM " + dayPattern;
	            dateString = year + " " + month + " " + day;
	        } else if(monthName != null) {
	            if(monthName.length() == 3) dateFormatPattern = "yyyy MMM " + dayPattern;
	            else dateFormatPattern = "yyyy MMMM " + dayPattern;
	            dateString = year + " " + monthName + " " + day;
	        }

	        if(hour != null && minute != null) {
	            //TODO ampm
	            dateFormatPattern += " HH:mm";
	            dateString += " " + hour + ":" + minute;
	            if(second != null) {
	                dateFormatPattern += ":ss";
	                dateString += ":" + second;
	            }
	        }

	        if(!dateFormatPattern.equals("") && !dateString.equals("")) {
	            //TODO support different locales
	            SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatPattern.trim(), Locale.US);
	            try {
					date = dateFormat.parse(dateString.trim());
				} catch (ParseException e) {
					date = null;
				}
	        }
	    }

	    return date;
	}

	private static Matcher checkDatePattern(String regex, String text) {
	    Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	    return p.matcher(text);
	}
}
