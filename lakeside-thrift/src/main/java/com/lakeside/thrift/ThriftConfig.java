package com.lakeside.thrift;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author zhufb
 *
 */
public class ThriftConfig extends HashMap<String,String>{
	
	private static final long serialVersionUID = 1L;

	public ThriftConfig() {
		
	}

	public ThriftConfig(Iterator<Entry<String, String>> iterator) {
		while(iterator.hasNext()){
			java.util.Map.Entry<String, String> next = iterator.next();
			this.put(next.getKey(), next.getValue());
		}
	}
	
	public ThriftConfig(Map conf) {
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
		return Integer.valueOf(value);
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