package com.lakeside.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lakeside.core.utils.StringUtils;

import java.io.PrintStream;
import java.util.*;
import java.util.Map.Entry;

/**
 * 
 * represent the argument configure setting.
 * 
 * please used this class to parse the java command line arguments setting
 * 
 * @author houdejun
 *
 */
public class ArgOptions extends BaseOptions {

	/**
	 * constructor
	 */
	ArgOptions(){
	}
	
	public void parse(String[] args) {
		this.parse(args,true);
	}

	/**
	 * parse the arguments list
	 * @param args
	 */
	public void parse(String[] args, boolean force) {
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
		}
		if(force) {
			boolean fail = false;
			for (Entry<String, Option> en : _options.entrySet()) {
				Option option = en.getValue();
				if (option.required && !option.haveValued()) {
					fail = true;
					break;
				}
			}
			if (fail) {
				this.printHelp();
				throw new RuntimeException("Please input required arguments.");
			}
		}
	}
	
	/**
	 * add a new argument description
	 * @param name
	 * @param required
	 * @param help
	 */
	@Override
	public void addArgument(String name, boolean required, String help){
		_options.put(name, new Option(name,required,help));
	}
	
	/**
	 * add a new argument description
	 * @param name
	 * @param required
	 * @param help
	 */
	@Override
	public void addArgument(String name, String alias, boolean required, String help){
		_options.put(name, new Option(name, alias,required,help));
	}

	/**
	 * convert the options to a map object
	 * @return
	 */
	public Map<String, String> toMap() {
		Map<String, String> map = Maps.newLinkedHashMap();
		for (Entry<String, Option> entry : _options.entrySet()) {
			String key = entry.getKey();
			Option option = entry.getValue();
			if (option != null && option.haveValued()) {
				map.put(key, option.getValue());
			}
		}
		return map;
	}

	/**
	 * print help information
	 */
	public void printHelp(){
		this.printHelp(System.out);
	}
	
	/**
	 * print the help message
	 * @param out
	 */
	public void printHelp(PrintStream out){
		List<Entry<String, Option>> entries = Lists.newArrayList(_options.entrySet());
		Collections.sort(entries, new Comparator<Entry<String, Option>>() {
			public int compare(Entry<String, Option> o1, Entry<String, Option> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
        });
		for(Entry<String,Option> en:entries){
			Option option = en.getValue();
			out.println(option.toString());
		}
	}

}