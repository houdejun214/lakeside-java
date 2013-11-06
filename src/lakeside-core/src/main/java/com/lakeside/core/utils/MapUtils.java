package com.lakeside.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		if(obj == null||StringUtils.isEmpty(fieldChain)){
			return obj;
		}
		int index = fieldChain.indexOf("/");
		String key = fieldChain;
		String next = null;
		if(index >= 0){
			key = fieldChain.substring(0,index);
			next = fieldChain.substring(index + 1,fieldChain.length());
		}
		
		Object value = obj.get(key);
		if(value==null||JSONNull.getInstance().equals(value)){
			return null;
		}else if(value instanceof Map){
			return getInter((Map)value,next);
		}else if(value instanceof List){
			return getInter((List)value, next);
		}
		return value;
	}
	
	public static Object getInter(List list,String fieldChain){
		if(list == null||list.size() == 0||StringUtils.isEmpty(fieldChain)){
			return list;
		}
		List<Object> result = new ArrayList<Object>();
		Iterator iterator = list.iterator();
		while(iterator.hasNext()){
			Object next = iterator.next();
			if(next instanceof Map){
				result.add(getInter((Map)next, fieldChain));
			}
		}
		return result;
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
	
	/**
	 * transform the nested data to single-deck data.
	 * @param data
	 * @return
	 */
	public static Map<String,Object> convertToSingleDeck(Map<String,Object> data){
		if(data==null){
			return data;
		}
		Map<String,Object> result = new HashMap<String,Object>();
		transformChildMapToSingleDeck(result,null,data);
		return result;
	}
	
	/**
	 * transform child array data to single deck
	 * @param data
	 * @param fatherKeyName
	 * @param listData
	 */
	private static void transformChildListToSingleDeck(Map<String, Object> data, String fatherKeyName,
			List<Object> listData) {
		if(listData!=null && listData.size()>0){
			for(int i=0;i<listData.size();i++){
				String newKeyName = fatherKeyName+"["+i+"]";
				Object value = listData.get(i);
				if(value != null){
					if(value instanceof Map){
						Map<String,Object> childData = (Map<String,Object>) value;
						transformChildMapToSingleDeck(data, newKeyName, childData);
					}else if(value instanceof List){
						List<Object> childData = (List<Object>) value;
						transformChildListToSingleDeck(data, newKeyName, childData);
					}else{
						data.put(newKeyName, value);
					}
				}
			}
		}
	}

	/**
	 * transform child Map data to single deck
	 * @param data
	 * @param fatherKeyName
	 * @param mapData
	 */
	private static void transformChildMapToSingleDeck(Map<String, Object> data, String fatherKeyName,
			Map<String,Object> mapData) {
		if(mapData!=null){
			Iterator<String> iterator = mapData.keySet().iterator();
			while(iterator.hasNext()){
				String keyName = iterator.next();
				Object value = mapData.get(keyName);
				String newKeyName = null;
				if(StringUtils.isEmpty(fatherKeyName)){
					newKeyName = keyName;
				}else{
					newKeyName = fatherKeyName+"."+keyName;
				}
				if(value != null){
					if(value instanceof Map){
						Map<String,Object> childData = (Map<String,Object>) value;
						transformChildMapToSingleDeck(data, newKeyName, childData);
					}else if(value instanceof List){
						List<Object> childData = (List<Object>) value;
						transformChildListToSingleDeck(data, newKeyName, childData);
					}else{
						data.put(newKeyName, value);
					}
				}
			}
		}
	}
	
	/**
	 * transform the single-deck data to nested data.
	 * @param data
	 * @return
	 */
	public static Map<String,Object> convertToNestedData(Map<String,Object> data){
		if(data==null){
			return data;
		}
		Map<String,Object> result = new HashMap<String,Object>();
		Object[] keys = data.keySet().toArray();
		List<Object> asList = Arrays.asList(keys);
		
		ComparatorKeys comp = new ComparatorKeys();
		Collections.sort(asList,comp);
		for(int i = 0; i<asList.size(); i++)  
		{    
			String key = StringUtils.valueOf(asList.get(i));
			Object value = data.get(key);
			transformSingleDeckToNestedMap(result, key, value);
		}
		return result;
	}
	
	/**
	 * recursive convert a map object to a standard JSONObject
	 * @param map
	 * @return
	 */
	public static Map cleanMap(Map map){
		Map<String,Object> result = new HashMap<String, Object>();
		Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String, Object> next = iterator.next();
			String key = next.getKey();
			Object value = next.getValue();
			if(value==null){
				result.put(key, null);
			}else if(value instanceof Map){
				result.put(key, cleanMap((Map)value));
			}else if (value instanceof List){
				result.put(key, cleanList((List)value));
			}else{
				if(StringUtils.isEmpty(String.valueOf(value))||"null".equals(String.valueOf(value))){
					result.put(key, null);
				}else{
					result.put(key, value);
				}
			}
		}
		return result;
	}
	
	/**
	 *  
	 * @param list
	 * @return
	 */
	public static List cleanList(List list){
		List result = new ArrayList();
		for(Object obj: list){
			if(obj instanceof Map){
				result.add(cleanMap((Map)obj));
			}else if (obj instanceof List){
				result.add(cleanList((List)obj));
			}else{
				result.add(obj);
			}
		}
		return result;
	}
	
	/**
	 * transform the single-deck data to nested map data
	 * @param father
	 * @param key
	 * @param value
	 * @return
	 */
	private static Map<String,Object> transformSingleDeckToNestedMap(Map<String,Object> father,String key,Object value){
		Pattern p = Pattern.compile("\\[[0-9]+\\]|\\.");
		Matcher m = p.matcher(key);
		String mainKey = null;
		String newKey = null;
		if(m.find()){
			//has child nested
			int start = m.start();
			int end = m.end();
			mainKey = key.substring(0, start);
			newKey = key.substring(end);
			String group = m.group();
			if(".".equals(group)){
				//map
				Map<String,Object> mapValue = new HashMap<String,Object>();
				if(father.containsKey(mainKey)){
					Object v=father.get(mainKey);
					if(v!=null && v instanceof Map){
						mapValue = (Map<String,Object>)v;
					}
				}
				mapValue = transformSingleDeckToNestedMap(mapValue,newKey,value);
				father.put(mainKey, mapValue);
			}else{
				//list
				int arrayNum =Integer.valueOf(group.substring(1, group.length()-1));
				List<Object> listValue = new ArrayList<Object>();
				if(father.containsKey(mainKey)){
					Object v=father.get(mainKey);
					if(v!=null && v instanceof List){
						listValue = (List<Object>)v;
					}
				}
				if(StringUtils.isEmpty(newKey)){
					if(listValue.size()>arrayNum){
						listValue.set(arrayNum, value);
					}else{
						listValue.add(value);
					}
				}else{
					listValue = transformSingleDeckToNestedList(listValue,arrayNum,newKey,value);
				}
				father.put(mainKey, listValue);
			}
		}else{
			mainKey = key;
			father.put(mainKey, value);
		}
		return father;
	}
	
	/**
	 * transform the single-deck data to nested List data
	 * @param father
	 * @param keyNum
	 * @param key
	 * @param value
	 * @return
	 */
	private static List<Object> transformSingleDeckToNestedList(List<Object> father,int keyNum,String key,Object value){
		Pattern p = Pattern.compile("\\[[0-9]+\\]|\\.");
		Matcher m = p.matcher(key);
		String newKey = null;
		if(m.find()){
			//has child nested
			int start = m.start();
			int end = m.end();
			newKey = key.substring(end);
			String group = m.group();
			if(".".equals(group)){
				//map
				Map<String,Object> mapValue = null;
				Object v=null;
				if(father.size()>keyNum){
					v = father.get(keyNum);
				}
				if(v!=null && v instanceof Map){
					mapValue = (Map<String,Object>)v;
				}else{
					mapValue =new HashMap<String,Object>();
				}
				mapValue = transformSingleDeckToNestedMap(mapValue,newKey,value);
				if(father.size()>keyNum){
					father.set(keyNum, mapValue);
				}else{
					father.add(mapValue);
				}
			}else{
				//list
				int arrayNum =Integer.valueOf(group.substring(1, group.length()-1));
				Object v = null;
				if(father.size()>keyNum){
					v = father.get(keyNum);
				}
				List<Object> listValue = null;
				if(v!=null && v instanceof List){
					listValue = (List<Object>)v;
				}else{
					listValue =new ArrayList<Object>();
				}
				if(StringUtils.isEmpty(newKey)){
					if(listValue.size()>arrayNum){
						listValue.set(arrayNum, value);
					}else{
						listValue.add(value);
					}
				}else{
					listValue = transformSingleDeckToNestedList(listValue,arrayNum,newKey,value);
				}
				
				if(father.size()>keyNum){
					father.set(keyNum, listValue);
				}else{
					father.add(listValue);
				}
			}
		}else{
			if(father.size()>keyNum){
				father.set(keyNum, value);
			}else{
				father.add(value);
			}
		}
		return father;
	}
	
	public static String getString(Map<String,Object> map,String key){
		if(!map.containsKey(key)||map.get(key) == null){
			return null;
		}
		return map.get(key).toString();
	}
	
	public static Integer getInt(Map<String,Object> map,String key){
		if(!map.containsKey(key)||map.get(key) == null){
			return null;
		}
		String str = map.get(key).toString();
		if(!StringUtils.isNum(str)){
			return null;
		}
		return Integer.parseInt(str);
	}

	public static Long getLong(Map<String,Object> map,String key){
		if(!map.containsKey(key)||map.get(key) == null){
			return null;
		}
		String str = map.get(key).toString();
		if(!StringUtils.isNum(str)){
			return null;
		}
		return Long.parseLong(str);
	}
	
	/**
	 * sort a map by value
	 * 
	 * @param map
	 * @return
	 */
	public static <K,V extends Comparable<? super V>> SortedSet<Entry<K, V>> sortedByValues(Map<K,V> map,final boolean asc) {
	    SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
	        new Comparator<Map.Entry<K,V>>() {
	            public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
	                int compareTo = e1.getValue().compareTo(e2.getValue());
	                int direct = 1;
	                if(!asc){
	                	direct = -1;
	                }
					return compareTo*direct;
	            }
	        }
	    );
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
	}
}

class ComparatorKeys implements Comparator<Object>{
	 public int compare(Object arg0, Object arg1) {
		try{
			String a0 = StringUtils.valueOf(arg0);
			String a1 = StringUtils.valueOf(arg1);
			return compareStringKey(a0,a1);
		}catch(Exception e){
			return 1;
		}
		
	 }
	 
	private int compareStringKey(String key1, String key2) {
		Pattern p = Pattern.compile("\\[[0-9]+\\]");
		Matcher m1 = p.matcher(key1);
		Matcher m2 = p.matcher(key2);
		if (m1.find() && m2.find()) {
			// key1
			int start1 = m1.start();
			int end1 = m1.end();
			int start2 = m2.start();
			int end2 = m2.end();
			//split every key like this format: mainkey+[num]+newkey
			//compare mainkey ,if they are different,return result
			String mainKey1 = key1.substring(0, start1);
			String mainKey2 = key2.substring(0, start2);
			int mainKeyComp = mainKey1.compareTo(mainKey2);
			if(mainKeyComp!=0){
				return mainKeyComp;
			}
			
			//compare num ,if they are different,return result
			String group1 = m1.group();
			int num1 =Integer.valueOf(group1.substring(1, group1.length()-1)) ;
			String group2 = m2.group();
			int num2 = Integer.valueOf(group2.substring(1, group2.length()-1));
			if(num1!=num2){
				return num1 - num2;
			}
			
			//cant find result,continue to compare newkey
			String newKey1 = key1.substring(end1);
			String newKey2 = key2.substring(end2);
			return compareStringKey(newKey1,newKey2);
		}else{
			return key1.compareTo(key2);
		}
	}
	
}
