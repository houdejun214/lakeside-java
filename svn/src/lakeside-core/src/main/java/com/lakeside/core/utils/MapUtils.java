package com.lakeside.core.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONNull;

public class MapUtils {
	
	/**
	 * get the internal value from a map by the key name,
	 * the key name can be a name queue which contain the nested keys
	 * 
	 * @param obj  {name:'name1',group:{grouname:'group1',order:1}}
	 * @param fieldChain "name", "group/groupname";
	 * @return
	 */
	public static Object getInter(Map obj,String fieldChain){
		String[] split = fieldChain.split("/");
		Map cur = obj;
		int length = split.length;
		Object value = null;
		for(int i=0;i<length&&cur!=null;i++){
			String key = split[i];
			value = cur.get(key);
			if(value==null||JSONNull.getInstance().equals(value)){
				return null;
			}
			cur = null;
			if(value instanceof Map){
				cur = (Map)value;
			}else if(value instanceof List){
				List array = (List)value;
				if(array.size() > 0 &&array.get(0) instanceof Map) {
					cur = (Map)array.get(0);
				}
			}else if(i<length-1){
				throw new RuntimeException(key+" is not a valid map object");
			}
		}
		return value;
	}

	public static Object getField(Map<String,Object> data,String name){
		Iterator<Entry<String, Object>> iterator = data.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String, Object> next = iterator.next();
			String key = next.getKey();
			Object value = next.getValue();
			if(key.equals(name)){
				return value;
			}
			if(value instanceof Map){
				value = getField((Map)value,name);
				if(value!=null){
					return value;
				}
			}else if(value instanceof List){
				value = getList((List)value,name);
				if(value!=null){
					return value;
				}
			}
		}
		return null;
	}
	
	private static Object getList(List<?> list,String name) {
		Object result = null;
		for(Object item:list){
			if(item instanceof Map){
				result = getField((Map)item,name);
				if(result!=null){
					return result;
				}
			}else if(item instanceof List){
				result = getList((List)item,name);
				if(result!=null){
					return result;
				}
			}
		}
		return null;
	}
	
	public static String getInterString(Map obj,String fieldChain){
		Object inter = getInter(obj,fieldChain);
		if(inter==null){
			return null;
		}
		return inter.toString();
	}
}
