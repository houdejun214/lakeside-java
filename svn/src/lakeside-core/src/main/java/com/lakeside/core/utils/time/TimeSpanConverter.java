package com.lakeside.core.utils.time;

import org.apache.commons.beanutils.Converter;

public class TimeSpanConverter implements Converter{

	public Object convert(Class type, Object value) {
		if(value!=null){
			long l_value = Long.valueOf(value.toString());
			return new TimeSpan(l_value);
		}else {
			return new TimeSpan();
		}
	}

}
