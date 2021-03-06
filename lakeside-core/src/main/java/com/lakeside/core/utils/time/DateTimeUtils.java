package com.lakeside.core.utils.time;

import com.lakeside.core.utils.Assert;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationFieldType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeUtils {
	private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

	public static String format(Date date, String parttern) {
		if (date == null) {
			return "";
		}
		if (parttern == null || parttern.equals("")) {
			return "";
		}
		DateTime time = new DateTime(date);
		DateTimeFormatter formatter = DateTimeFormat.forPattern(parttern);
		return formatter.print(time);
	}

	public static String formatGMT(Date date, String parttern) {
		if (date == null) {
			return "";
		}
		if (parttern == null || parttern.equals("")) {
			return "";
		}
		DateTime time = new DateTime(date);
		time.withZone(DateTimeZone.UTC);
		DateTimeFormatter formatter = DateTimeFormat.forPattern(parttern);
		return formatter.print(time);
	}

	public static Date parse(String str, String parttern) {
		if (str == null) {
			return null;
		}
		if (parttern == null || parttern.equals("")) {
			return null;
		}
		DateTimeFormatter formater = DateTimeFormat.forPattern(parttern);
		return formater.parseDateTime(str).toDate();
	}

	public static Long getCurrentTime() {
		return new DateTime().getMillis();
	}

	public static Boolean between(Date test, Date start, Date end) {
		Assert.notNull(test);
		Assert.notNull(start);
		Assert.notNull(end);
		if (start.after(end)) {
			throw new IllegalArgumentException(
					"start date must is before the end");
		}
		DateTime time = new DateTime(test);
		if (time.isBefore(start.getTime()) || time.isAfter(end.getTime())) {
			return false;
		}
		return true;
	}

	/**
	 * compare two date object
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int compareDate(Date date1, Date date2) {
		if (date1 == null && date2 != null) {
			return -1;
		} else if (date1 != null && date2 == null) {
			return 1;
		} else {
			return date1.compareTo(date2);
		}
	}

	/**
	 * Adds or subtracts the specified amount of time to the given calendar
	 * field, based on the calendar's rules. For example, to subtract 5 days
	 * from the current time of the calendar, you can achieve it by calling:
	 * add(DateNow,Calendar.DAY_OF_MONTH, -5).
	 * 
	 * @param time
	 * @param field
	 * @param amount
	 * @return
	 */
	public static Date add(Date time, DurationFieldType field, int amount) {
		DateTime instance = new DateTime(time);
		return instance.withFieldAdded(field, amount).toDate();
	}

	/**
	 * get a between time instance base on the weight
	 * 
	 * @param lower
	 * @param upper
	 * @param weight
	 * @return
	 */
	public static Date getBetweenTime(Date lower, Date upper, float weight) {
		long time = (long) (lower.getTime() * weight + upper.getTime()
				* (1 - weight));
		return new Date(time);
	}

	public static Date getTimeFromUnixTime(Long time) {
		Date t = new Date(time * 1000);
		if (t.compareTo(new Date()) > 0) {
			t = new Date(time);
		}
		return t;
	}

	public static Date getTimeFromUnixTime(String time) {
		return getTimeFromUnixTime(Long.parseLong(time));
	}

	public static Date getEndOfDay(Date now) {
		DateTime time = new DateTime(now);
		return time.withTime(23, 59, 59, 0).toDate();
	}

	public static Date getBeginOfDay(Date now) {
		DateTime time = new DateTime(now);
		return time.withTimeAtStartOfDay().toDate();
	}

	public static long getUnixTime(Date date) {
		Calendar instance = Calendar.getInstance();
		DateTime time = new DateTime(date);
		return time.getMillis()/1000;
	}

	/**
	 * 得到某年某周的第一天
	 * 
	 * @param year
	 * @param week
	 * @return
	 */
	public static Date getFirstDayOfWeek(int year, int week) {
		DateTime time = new DateTime().withYear(year);
		return time.withWeekOfWeekyear(week).dayOfWeek().roundFloorCopy().toDate();
	}

	/**
	 * 得到某年某周的最后一天
	 * 
	 * @param year
	 * @param week
	 * @return
	 */
	public static Date getLastDayOfWeek(int year, int week) {
		DateTime time = new DateTime().withYear(year);
		return time.withWeekOfWeekyear(week).dayOfWeek().roundCeilingCopy().toDate();
	}

	/**
	 * 得到某年某月的第一天
	 * 
	 * @param year
	 * @param month
	 * @return
	 */
	public static Date getFirstDayOfMonth(int year, int month) {
		DateTime time = new DateTime().withYear(year);
		return time.withMonthOfYear(month).dayOfMonth().roundFloorCopy().toDate();
	}

	public static Date getFirstDayOfMonth(Date date) {
		DateTime time = new DateTime(date);
		return time.dayOfMonth().roundFloorCopy().toDate();
	}
	/**
	 * 得到某年某月的最后一天
	 * 
	 * @param year
	 * @param month
	 * @return
	 */
	public static Date getLastDayOfMonth(int year, int month) {
		DateTime time = new DateTime().withYear(year);
		return time.withMonthOfYear(month).dayOfMonth().roundCeilingCopy().toDate();
	}

	/**
	 * 得到某年某季度第一天
	 * 
	 * @param year
	 * @param quarter
	 * @return
	 */
	public static Date getFirstDayOfQuarter(int year, int quarter) {
		int month = 0;
		if (quarter > 4) {
			return null;
		} else {
			month = (quarter - 1) * 3 + 1;
		}
		return getFirstDayOfMonth(year, month);
	}

	/**
	 * 得到某年某季度最后一天
	 * 
	 * @param year
	 * @param quarter
	 * @return
	 */
	public static Date getLastDayOfQuarter(int year, int quarter) {
		int month = 0;
		if (quarter > 4) {
			return null;
		} else {
			month = quarter * 3;
		}
		return getLastDayOfMonth(year, month);
	}

	/**
	 * 得到某年第一天
	 * 
	 * @param year
	 * @return
	 */
	public static Date getFirstDayOfYear(int year) {
		DateTime time = new DateTime().withYear(year);
		return time.dayOfYear().roundFloorCopy().toDate();
	}

	/**
	 * 得到某年最后一天
	 * 
	 * @param year
	 * @return
	 */
	public static Date getLastDayOfYear(int year) {
		DateTime time = new DateTime().withYear(year);
		return time.dayOfYear().roundCeilingCopy().toDate();
	}
}
