package com.lakeside.core;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dejun on 24/11/14.
 */
public abstract class BaseOptions implements Options {

	protected static final String NO_NAME_PARAMTER = "___";

	protected Map<String, Option> _options = Maps.newHashMap();

	/**
	 * put option into
	 * @param key
	 * @param value
	 */
	void put(String key, String value){
		Option option = this._options.get(key);
		if (option == null) {
			option = new Option(key,false,"");
			this._options.put(key, option);
		}
		option.setValue(value);
	}

	/**
	 * put option into.
	 * @param key
	 * @param option
	 */
	void put(String key, Option option){
		this._options.put(key, option);
		if(option.haveAlias()){
			this._options.put(option.alias, option);
		}
	}

	public boolean containsKey(String key){
		return this._options.containsKey(key);
	}

	public void duplicateParameter(String key1, String key2){
		if(this._options.containsKey(key1)&&!this._options.containsKey(key2)){
			this._options.put(key2, this._options.get(key1));
		}
		if(this._options.containsKey(key2)&&!this._options.containsKey(key1)){
			this._options.put(key1, this._options.get(key2));
		}
	}

	@Override
	public Long getLong(String key, long defaultVal){
		String value = this.get(key);
		if(value==null){
			return defaultVal;
		}
		return Long.valueOf(value);
	}

	@Override
	public Integer getInt(String key, int defaultValue){
		String value = this.get(key);
		if(value==null){
			return defaultValue;
		}
		return Integer.valueOf(value);
	}

	@Override
	public float getFloat(String key, float defaultValue){
		String value = this.get(key);
		if(value==null){
			return defaultValue;
		}
		return Float.valueOf(value);
	}

	@Override
	public double getDouble(String key, double defaultValue){
		String value = this.get(key);
		if(value==null){
			return defaultValue;
		}
		return Double.valueOf(value);
	}

	@Override
	public float getBigDecimal(String key, float defaultValue){
		String value = this.get(key);
		if(value==null){
			return defaultValue;
		}
		return Float.valueOf(defaultValue);
	}

	@Override
	public String get(String key, boolean required){
		String value = this.get(key);
		if(value==null && required){
			throw new RuntimeException("arguments --"+key+" must be provided.");
		}
		return value;
	}

	@Override
	public boolean getBoolean(String key, boolean defaultValue){
		String value = this.get(key);
		if(value==null){
			return defaultValue;
		}
		return Boolean.valueOf(value);
	}

	@Override
	public String get(String key, String defaultValue){
		String value = this.get(key);
		if(value==null){
			return defaultValue;
		}
		return value;
	}

	/**
	 * get the input parameter which have't been specified a name
	 * @return
	 */
	@Override
	public String get(){
		return this.get(NO_NAME_PARAMTER,"");
	}

	//获取字符串数组，
	@Override
	public String[] getStringArray(String key, String split){
		String value = this.get(key);
		if(value==null){
			return null;
		}
		return value.split(split);
	}

	@Override
	public String get(String key){
		Option option = this._options.get(key);
		if(option == null){
			return null;
		}
		return option.getValue();
	}

	@Override
	public boolean haveArg(String name){
		if(this._options.containsKey(name) && this._options.get(name).haveValued()){
			return true;
		}
		return false;
	}

	/**
	 * check if all required options have been set.
	 * @return
	 */
	public boolean haveAllRequired(){
		Map<String, String> map = Maps.newLinkedHashMap();
		for (Map.Entry<String, Option> entry : _options.entrySet()) {
			Option option = entry.getValue();
			if (option != null && option.required && !option.haveValued()) {
				return false;
			}
		}
		return true;
	}

	protected static class Option implements Comparable<Option>{
		private String name;
		protected boolean required = false;
		private String help;
		protected String alias;
		private String value;
		public Option(String name, boolean required, String help) {
			this.name = name;
			this.required = required;
			this.help = help;
		}

		public Option(String name, String alias, boolean required, String help) {
			this.name = name;
			this.alias = alias;
			this.required = required;
			this.help = help;
		}

		/**
		 * check if the option have alias settings
		 * @return
		 */
		public boolean haveAlias(){
			return alias!=null && !"".equals(alias);
		}

		/**
		 * check if the options have values
		 * @return
		 */
		public boolean haveValued() {
			return this.value != null && !"".equals(value);
		}

		@Override
		public String toString() {
			String aliasInfo = "";
			if(alias!=null&&!"".equals(alias)){
				aliasInfo=",--"+alias;
			}
			if(required){
				return "--"+name+aliasInfo+"\t\t\t"+help;
			}else{
				return "[--"+name+aliasInfo+"]\t\t\t"+help;
			}
		}

		@Override
		public int compareTo(Option o) {
			if(!this.required ^ o.required){
				return 0;
			}
			if(this.required){
				return -1;
			}else{
				return 1;
			}
		}


		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
}
