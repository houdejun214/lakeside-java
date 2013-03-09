
package com.lakeside.core.utils.time;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.converters.DateTimeConverter;

import com.lakeside.core.utils.StringUtils;

public class StrToDateConverter extends DateTimeConverter {
	
	private static final Pattern DatePattern = Pattern.compile("\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}");

	public StrToDateConverter(){
		
	}
	
	public StrToDateConverter(Object defaultValue){
		 super(defaultValue);
	}
	
	protected Class<?> getDefaultType() {
        return Date.class;
    }
	@Override
	protected Object convertToType(Class targetType, Object value) throws Exception {
		if(value==null){
			return null;
		}
		String strValue = value.toString();
		if(strValue.equals("")){
			return null;
		}
		if(StringUtils.isNum(strValue)){
			return DateTimeUtils.getTimeFromUnixTime(strValue);
		}else if(DatePattern.matcher(strValue).matches()){
			SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			return format.parse(strValue);
		}else{
			return new Date(strValue);
		}
	}

	@Override
	protected Object handleMissing(Class type) {
		return null;
	}
}
