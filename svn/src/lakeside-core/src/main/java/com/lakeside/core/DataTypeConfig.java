package com.lakeside.core;

import java.util.HashMap;
import java.util.Map;

import com.lakeside.core.regex.CommonPattern;

public class DataTypeConfig {
	private static final Map<DataType, String> patterns = new HashMap<DataType, String>();
	private static final Map<DataType, Integer> lengths = new HashMap<DataType, Integer>();
	static {
		patterns.put(DataType.Cn, CommonPattern.CN);
		patterns.put(DataType.Date, CommonPattern.DATE);
		patterns.put(DataType.DateTime, CommonPattern.DATETIME);
		patterns.put(DataType.Decimal, CommonPattern.DECIMAL);
		patterns.put(DataType.Digit, CommonPattern.DIGIT);
		patterns.put(DataType.Email, CommonPattern.EMAIL);
		patterns.put(DataType.En, CommonPattern.EN);
		patterns.put(DataType.HomePhone, CommonPattern.HOMEPHONE);
		patterns.put(DataType.IdCard, CommonPattern.IDCARD);
		patterns.put(DataType.Int, CommonPattern.INT);
		patterns.put(DataType.MobilePhone, CommonPattern.MOBILEPHONE);
		patterns.put(DataType.Number, CommonPattern.NUMBER);
		patterns.put(DataType.PostCode, CommonPattern.POSTCODE);
		patterns.put(DataType.Time, CommonPattern.TIME);
		patterns.put(DataType.Year, CommonPattern.YEAR);
		patterns.put(DataType.YearMonth, CommonPattern.YEARMONTH);
	}
	static {
		lengths.put(DataType.Date, 8);
		lengths.put(DataType.DateTime, 17);
		lengths.put(DataType.HomePhone, 13);
		lengths.put(DataType.IdCard, 18);
		lengths.put(DataType.MobilePhone, 11);
		lengths.put(DataType.PostCode, 6);
		lengths.put(DataType.Time, 8);
		lengths.put(DataType.Year, 4);
		lengths.put(DataType.YearMonth, 6);
	}

	public static String getPatternStr(DataType dataType) {
		if (!patterns.containsKey(dataType)) {
			return "";
		}
		return patterns.get(dataType);
	}

	public static int getLength(DataType dataType) {
		if (!lengths.containsKey(dataType)) {
			return -1;
		}
		return lengths.get(dataType);
	}
}
