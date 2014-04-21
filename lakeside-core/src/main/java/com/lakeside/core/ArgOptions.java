package com.lakeside.core;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
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
	
	private static final String NO_NAME_PARAMTER = "___";
	
	private Map<String, String> map = new HashMap<String,String>();
	
	private Map<String, ArgOption> options = new HashMap<String,ArgOption>();
	
	/**
	 * constructor
	 */
	public ArgOptions(){
	}
	
	/**
	 * constructor
	 */
	public ArgOptions(String[] args){
		parse(args);
	}

	/**
	 * parse the arguments list
	 * @param args
	 */
	public void parse(String[] args) {
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
				}else{
					//no name specified.
					this.put(NO_NAME_PARAMTER,arg);
				}
			}
			for(Entry<String,ArgOption> en:options.entrySet()){
				String key = en.getKey();
				ArgOption option = en.getValue();
				if(option.required && !map.containsKey(key) && !map.containsKey(option.alias)){
					this.printHelp();
					throw new RuntimeException("parameter "+key+" is required.");
				}
				if(option.haveAlias()){
					duplicateParameter(key,option.alias);
				}
			}
		}
	}
	
	/**
	 * add a new argument description
	 * @param name
	 * @param required
	 * @param help
	 */
	public void addArgument(String name,boolean required,String help){
		options.put(name, new ArgOption(name,required,help));
	}
	
	/**
	 * add a new argument description
	 * @param name
	 * @param required
	 * @param help
	 */
	public void addArgument(String name,String alias,boolean required,String help){
		options.put(name, new ArgOption(name, alias,required,help));
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
	
	/**
	 * get the input parameter which have't been specified a name
	 * @return
	 */
	public String get(){
		return this.get(NO_NAME_PARAMTER,"");
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
	
	public Map<String, String> toMap() {
		return map;
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
	
	private void duplicateParameter(String key1,String key2){
		if(this.map.containsKey(key1)&&!this.map.containsKey(key2)){
			this.map.put(key2, this.map.get(key1));
		}
		if(this.map.containsKey(key2)&&!this.map.containsKey(key1)){
			this.map.put(key1, this.map.get(key2));
		}
	}
	
	public void printHelp(){
		this.printHelp(System.out);
	}
	
	/**
	 * print the help message
	 * @param out
	 */
	public void printHelp(PrintStream out){
		ArrayList<Entry<String, ArgOption>> entries = Lists.newArrayList(options.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, ArgOption>>() {
			public int compare(Entry<String, ArgOption> o1, Entry<String, ArgOption> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
        });
		for(Entry<String,ArgOption> en:entries){
			ArgOption option = en.getValue();
			out.println(option.toString());
		}
	}
	
	private static class ArgOption implements Comparable<ArgOption>{
		private String name;
		private boolean required = false;
		private String help;
		private String alias;
		public ArgOption(String name,boolean required, String help) {
			this.name = name;
			this.required = required;
			this.help = help;
		}
		
		public ArgOption(String name,String alias,boolean required, String help) {
			this.name = name;
			this.alias = alias;
			this.required = required;
			this.help = help;
		}
		
		public boolean haveAlias(){
			return alias!=null&&!"".equals(alias);
		}

		@Override
		public String toString() {
			String aliasInfo = "";
			if(alias!=null&&!"".equals(alias)){
				aliasInfo=",--"+alias;
			}
			if(required){
				return "--"+name+aliasInfo+"\t\t"+help;
			}else{
				return "[--"+name+aliasInfo+"]\t\t"+help;
			}
		}

		@Override
		public int compareTo(ArgOption o) {
			if(!this.required ^ o.required){
				return 0;
			}
			if(this.required){
				return -1;
			}else{
				return 1;
			}
		}
	}
}