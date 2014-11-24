package com.lakeside.core;

/**
 * Created by dejun on 24/11/14.
 */
public interface Options {

	void addArgument(String name, boolean required, String help);

	void addArgument(String name, String alias, boolean required, String help);

	Long getLong(String key, long defaultVal);

	Integer getInt(String key, int defaultValue);

	float getFloat(String key, float defaultValue);

	double getDouble(String key, double defaultValue);

	float getBigDecimal(String key, float defaultValue);

	String get(String key, boolean required);

	boolean getBoolean(String key, boolean defaultValue);

	String get(String key, String defaultValue);

	String get();

	//获取字符串数组，
	String[] getStringArray(String key, String split);

	boolean haveArg(String name);

	String get(String key);
}
