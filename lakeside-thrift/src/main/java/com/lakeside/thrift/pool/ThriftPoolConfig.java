package com.lakeside.thrift.pool;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 */
public class ThriftPoolConfig extends HashMap<String,String>{
	
	private static final long serialVersionUID = 1L;

	public ThriftPoolConfig() {
		
	}

	public ThriftPoolConfig(Iterator<Entry> iterator) {
		while(iterator.hasNext()){
			Entry<?, ?> next = iterator.next();
			this.put(String.valueOf(next.getKey()), String.valueOf(next.getValue()));
		}
	}
	
	public ThriftPoolConfig(Map conf) {
		this.putAll(conf);
	}

	public Long getLong(String key){
		String value = this.get(key);
		if(value==null){
			return null;
		}
		return Long.valueOf(value);
	}
	
	public int getInt(String key,int defaultValue){
		String value = this.get(key);
		if(value==null){
			return defaultValue;
		}
		return Integer.valueOf(value).intValue();
	}
	
	public BigDecimal getBigDecimal(String key){
		String value = this.get(key);
		if(value==null){
			return null;
		}
		return new BigDecimal(value);
	}
	
	public Date getDate(String key){
		String value = this.get(key);
		if(value==null){
			return null;
		}
		if(value.equals("")){
			return null;
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return format.parse(value);
		} catch (ParseException e) {
			return null;
		}
	}
	
	public Boolean getBoolean(String key,boolean defaultValue){
		String value = this.get(key);
		if(value==null){
			return defaultValue;
		}
		return Boolean.parseBoolean(value);
	}
	
	public String get(String key,String defaultVal){
		String val = this.get(key);
		if(val==null){
			return defaultVal;
		}
		return val;
	}
}