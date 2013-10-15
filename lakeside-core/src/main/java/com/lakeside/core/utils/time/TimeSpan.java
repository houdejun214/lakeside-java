package com.lakeside.core.utils.time;

import java.util.Date;

public class TimeSpan {

	private long millispan;
	private long days;
	private long hours;
	private long minutes;
	private long seconds;
	private long millisecond;

	public long getMillispan() {
		return millispan;
	}

	public void setMillispan(long millispan) {
		this.millispan = millispan;
	}

	public long getDays() {
		return days;
	}

	public void setDays(long days) {
		this.days = days;
	}

	public long getHours() {
		return hours;
	}

	public void setHours(long hours) {
		this.hours = hours;
	}

	public long getMinutes() {
		return minutes;
	}

	public void setMinutes(long minutes) {
		this.minutes = minutes;
	}

	public long getSeconds() {
		return seconds;
	}

	public void setSeconds(long seconds) {
		this.seconds = seconds;
	}

	public long getMillisecond() {
		return millisecond;
	}

	public void setMillisecond(long millisecond) {
		this.millisecond = millisecond;
	}

	public TimeSpan() {
		this.days = 0;
		this.hours = 0;
		this.minutes = 0;
		this.seconds = 0;
		this.millisecond = 0;
		this.millispan = 0;
	}
	
	public TimeSpan(long timespan) {
		init(timespan);
	}
	
	public TimeSpan(String timespan) {
		long l_timespan= Long.valueOf(timespan);
		init(l_timespan);
	}

	public TimeSpan(Date startDate, Date endDate) {
		if(startDate.after(endDate)){
			throw new IllegalArgumentException("start date must before the end date!");
		}
		long span = endDate.getTime() - startDate.getTime();
		init(span);
	}

	private void init(long l) {
		long day = l / (24 * 60 * 60 * 1000);
		long hour = (l / (60 * 60 * 1000) - day * 24);
		long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
		long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
		this.days = day;
		this.hours = hour;
		this.minutes = min;
		this.seconds = s;
		this.millisecond = l % 1000;
		this.millispan = l;
	}

	public TimeSpan add(TimeSpan other) {
		long newspan = this.millispan + other.getMillispan();
		this.init(newspan);
		return this;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		if(days>0){
			return days+","+hours+":"+minutes+":"+seconds;
		}else{
			return hours+":"+minutes+":"+seconds;
		}
	}
	
	public String toTimeSpanValue() {
		return String.valueOf(this.millispan);
	}
	
}
