package com.lakeside.core;

import java.util.HashMap;

import com.lakeside.core.utils.StringUtils;

/**
 * 
 * represent the argument configure setting.
 * 
 * please used this class to parse the java command line arguments setting
 * 
 * @author houdejun
 *
 */
public class ArgOptions {
	
	private HashMap<String, String> map = new HashMap<String,String>();
	
	/**
	 * constructor
	 */
	public ArgOptions(String[] args){
		if(args!=null){
			int argLength = args.length;
			for( int i=0;i<argLength;i++){
				String arg = args[i];
				if(arg.startsWith("-")) {
					arg = StringUtils.trimLeadingCharacter(arg, '-');
					if(arg.indexOf("=")>-1){
						String[] splits = arg.split("=");
						if(splits!=null && splits.length==2){
							this.put(splits[0], splits[1]);
						}
					}else if((i+1)<argLength && !args[i+1].startsWith("-")){
							this.put(arg,args[i+1]);
							i++;
					}else{
						this.put(arg,"true");
					}
				}
			}
		}
	}

	public Long getLong(String key,long defaultVal){
		String value = this.get(key);
		if(value==null){
			return defaultVal;
		}
		return Long.valueOf(value);
	}
	
	public Integer getInt(String key,int defaultValue){
		String value = this.get(key);
		if(value==null){
			return defaultValue;
		}
		return Integer.valueOf(value);
	}
	
	public float getFloat(String key,float defaultValue){
		String value = this.get(key);
		if(value==null){
			return defaultValue;
		}
		return Float.valueOf(value);
	}
	
	public double getDouble(String key,double defaultValue){
		String value = this.get(key);
		if(value==null){
			return defaultValue;
		}
		return Double.valueOf(value);
	}
	
	public float getBigDecimal(String key,float defaultValue){
		String value = this.get(key);
		if(value==null){
			return defaultValue;
		}
		return Float.valueOf(defaultValue);
	}
	
	public String get(String key,boolean required){
		String value = this.get(key);
		if(value==null && required){
			throw new RuntimeException("arguments --"+key+" must be provided.");
		}
		return value;
	}
	
	public boolean getBoolean(String key,boolean defaultValue){
		String value = this.get(key);
		if(value==null){
			return defaultValue;
		}
		return Boolean.valueOf(value);
	}
	
	public String get(String key,String defaultValue){
		String value = this.get(key);
		if(value==null){
			return defaultValue;
		}
		return value;
	}
	
	//获取字符串数组，
	public String[] getStringArray(String key,String split){
		String value = this.get(key);
		if(value==null){
			return null;
		}
		return value.split(split);
	}
	
	public boolean haveArg(String name){
		if(this.containsKey(name)){
			return true;
		}
		return false;
	}
	
	private void put(String key,String value){
		this.map.put(key, value);
	}
	
	public String get(String key){
		return this.map.get(key);
	}
	
	private boolean containsKey(String key){
		return this.map.containsKey(key);
	}
}