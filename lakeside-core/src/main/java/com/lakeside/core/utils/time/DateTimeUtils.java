package com.lakeside.core.utils.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.lakeside.core.utils.Assert;

public class DateTimeUtils {
	private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

	public static String format(Date date, String parttern) {
		if (date == null) {
			return "";
		}
		if (parttern == null || parttern.equals("")) {
			return "";
		}
		SimpleDateFormat formater = new SimpleDateFormat(parttern,
				Locale.ENGLISH);
		return formater.format(date);
	}

	public static String formatGMT(Date date, String parttern) {
		if (date == null) {
			return "";
		}
		if (parttern == null || parttern.equals("")) {
			return "";
		}
		SimpleDateFormat formater = new SimpleDateFormat(parttern,
				Locale.ENGLISH);
		formater.setTimeZone(GMT);
		return formater.format(date);
	}

	public static Date parse(String str, String parttern) {
		if (str == null) {
			return null;
		}
		if (parttern == null || parttern.equals("")) {
			return null;
		}
		SimpleDateFormat formater = new SimpleDateFormat(parttern,
				Locale.ENGLISH);
		try {
			return formater.parse(str);
		} catch (ParseException e) {
			return null;
		}
	}

	public static Long getCurrentTime() {
		return new Date().getTime();
	}

	public static Boolean between(Date test, Date start, Date end) {
		Assert.notNull(test);
		Assert.notNull(start);
		Assert.notNull(end);
		if (start.after(end)) {
			throw new IllegalArgumentException(
					"start date must is before the end");
		}
		if (test.before(start) && test.after(end)) {
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
	public static Date add(Date time, int field, int amount) {
		Calendar instance = Calendar.getInstance();
		instance.setTime(new Date(time.getTime()));
		instance.add(field, amount);
		return instance.getTime();
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
		Calendar instance = Calendar.getInstance();
		instance.setTime(now);
		instance.set(Calendar.HOUR_OF_DAY, 23);
		instance.set(Calendar.MINUTE, 59);
		instance.set(Calendar.SECOND, 59);
		instance.set(Calendar.MILLISECOND, 0);
		return instance.getTime();
	}

	public static Date getBeginOfDay(Date now) {
		Calendar instance = Calendar.getInstance();
		instance.setTime(now);
		instance.set(Calendar.HOUR_OF_DAY, 0);
		instance.set(Calendar.MINUTE, 0);
		instance.set(Calendar.SECOND, 0);
		instance.set(Calendar.MILLISECOND, 0);
		return instance.getTime();
	}

	public static long getUnixTime(Date date) {
		Calendar instance = Calendar.getInstance();
		instance.setTime(date);
		long utc = Date.UTC(instance.get(Calendar.YEAR) - 1900,
				instance.get(Calendar.MONTH),
				instance.get(Calendar.DAY_OF_MONTH),
				instance.get(Calendar.HOUR_OF_DAY),
				instance.get(Calendar.MINUTE), instance.get(Calendar.SECOND));
		return utc;
	}

	/**
	 * 得到某年某周的第一天
	 * 
	 * @param year
	 * @param week
	 * @return
	 */
	public static Date getFirstDayOfWeek(int year, int week) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.WEEK_OF_YEAR, week);
		c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);// 设置周一
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.setFirstDayOfWeek(Calendar.MONDAY);
		return c.getTime();
	}

	/**
	 * 得到某年某周的最后一天
	 * 
	 * @param year
	 * @param week
	 * @return
	 */
	public static Date getLastDayOfWeek(int year, int week) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.WEEK_OF_YEAR, week);
		c.setFirstDayOfWeek(Calendar.MONDAY);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek() + 6); // Sunday
		return c.getTime();
	}

	/**
	 * 得到某年某月的第一天
	 * 
	 * @param year
	 * @param month
	 * @return
	 */
	public static Date getFirstDayOfMonth(int year, int month) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month - 1);
		c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	/**
	 * 得到某年某月的最后一天
	 * 
	 * @param year
	 * @param month
	 * @return
	 */
	public static Date getLastDayOfMonth(int year, int month) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month - 1);
		c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
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
		return getFirstDayOfQuarter(year, 1);
	}

	/**
	 * 得到某年最后一天
	 * 
	 * @param year
	 * @return
	 */
	public static Date getLastDayOfYear(int year) {
		return getLastDayOfQuarter(year, 4);
	}
}
