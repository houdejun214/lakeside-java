package com.lakeside.core.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

/**
 * some utils for operate Json object
 * 
 * @author houdj
 *
 */
public class JSONUtils {
	
	/**
	 * recursive convert a map object to a standard JSONObject
	 * @param map
	 * @return
	 */
	public static JSONObject map2JSONObj(Map<String,Object> map){
		JSONObject json = new JSONObject();
		Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String, Object> next = iterator.next();
			String key = next.getKey();
			Object value = next.getValue();
			if(value==null){
				json.put(key, null);
			}else if(value instanceof Map){
				json.put(key, map2JSONObj((Map)value));
			}else if (value instanceof List){
				json.put(key, list2JSONArray((List)value));
			}else{
				if(StringUtils.isEmpty(String.valueOf(value))||"null".equals(String.valueOf(value))){
					json.put(key, null);
				}else{
					json.put(key, value);
				}
			}
		}
		return json;
	}
	
	/**
	 * check a string is a valid json string
	 * @param string
	 * @return
	 */
	public static boolean isValidJSON(String string){
		if(string==null || string.equals("")){
			return false;
		}
		if(!string.startsWith("{") || !string.endsWith("}")){
			return false;
		}
		return true;
	}
	
	/**
	 * recursive convert a list object to a standard JSONArray
	 *  
	 * @param list
	 * @return
	 */
	public static JSONArray list2JSONArray(List<?> list){
		JSONArray array = new JSONArray();
		for(Object obj: list){
			if(obj instanceof Map){
				array.add(map2JSONObj((Map)obj));
			}else if (obj instanceof List){
				array.add(list2JSONArray((List)obj));
			}else{
				array.add(obj);
			}
		}
		return array;
	}
}
