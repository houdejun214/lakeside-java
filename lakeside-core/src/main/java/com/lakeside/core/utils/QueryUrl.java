package com.lakeside.core.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * http查询url类,该类会解析出url中的所有的查询参数，以方便进一步的url处理
 * 
 * @author hdj
 *
 */
public class QueryUrl {
	/**
	 * 基础Url，不包含参数部分的Url
	 */
	private String baseQueryUrl;
	
	private List<ParamKeyVal> paramList = new ArrayList<ParamKeyVal>();

	private Map<String,List<ParamKeyVal>> name2Params = new HashMap<String, List<ParamKeyVal>>();
	
	private Map<String,List<ParamKeyVal>> value2Params = new HashMap<String, List<ParamKeyVal>>();
	
	public String getBaseQueryUrl() {
		return baseQueryUrl;
	}
	
	public void setBaseQueryUrl(String baseQueryUrl) {
		this.baseQueryUrl = baseQueryUrl;
	}

	public Set<String> getNames(){
		return this.name2Params.keySet();
	}
	
	public Set<String> getValues(){
		return this.value2Params.keySet();
	}

	public void setParameter(String name,String value){
		if(this.name2Params.containsKey(name)){
			//reset the exists parameter
			List<ParamKeyVal> list = this.name2Params.get(name);
			if(list!=null && list.size()>0){
				ParamKeyVal first = list.get(0);
				first.setValue(value);
				this.removeParameter(list.subList(1, list.size()));
			}
		}else{
			// have no this parameter
			ParamKeyVal param = new ParamKeyVal(name,value);
			name2Params.put(name, Arrays.asList(new ParamKeyVal[] { param }));
			this.paramList.add(param);
		}
	}
	
	public String getOneParameter(String name){
		List<ParamKeyVal> list = name2Params.get(name);
		if(list!=null&& list.size()>0){
			return list.get(0).getValue();
		}
		return null;
	}
	
	public List<ParamKeyVal> getParameterByName(String name){
		return this.name2Params.get(name);
	}
	
	public List<ParamKeyVal> getParameterByValue(String value){
		return this.value2Params.get(value);
	}

	public void removeParameter(String key){
		List<ParamKeyVal> parameters = this.getParameterByName(key);
		removeParameter(parameters);
	}
	
	public void removeParameter(ParamKeyVal param){
		List<ParamKeyVal> list = this.name2Params.get(param.getKey());
		if(list!=null){
			list.remove(param);
		}
		list = this.value2Params.get(param.getValue());
		if(list!=null){
			list.remove(param);
		}
		this.paramList.remove(param);
	}
	
	public void removeParameter(List<ParamKeyVal> params){
		if(params==null || params.size()==0)
			return;
		for(ParamKeyVal param:params){
			List<ParamKeyVal> list = this.name2Params.get(param.getKey());
			if(list!=null){
				list.remove(param);
			}
			list = this.value2Params.get(param.getValue());
			if(list!=null){
				list.remove(param);
			}
		}
		this.paramList.removeAll(params);
	}
	
	public void removeParameterByValue(String value){
		List<ParamKeyVal> parameters = this.value2Params.remove(value);
		for(ParamKeyVal param:parameters){
			List<ParamKeyVal> list = this.name2Params.get(param.getKey());
			if(list!=null){
				list.remove(param);
			}
		}
		this.paramList.removeAll(parameters);
	}
	
	/**
	 * Parses an URL query string and returns a _map with the parameter values.
	 * The URL query string is the part in the URL after the first '?' character
	 * up to an optional '#' character. It has the format
	 * "name=value&name=value&...". The _map has the same structure as the one
	 * returned by javax.servlet.ServletRequest.getParameterMap(). A parameter
	 * name may occur multiple times within the query string. For each parameter
	 * name, the _map contains a string array with the parameter values.
	 * 
	 * @param s  an URL query string.
	 * @return a QueryUrl object,compose of a parameter _map containing parameter names as keys and parameter values as
	 *         _map values, and names list.
	 */
	public QueryUrl(String s){
		if (s == null)
			throw new IllegalArgumentException("Query url is empty");
		int index = s.indexOf("?");
		this.baseQueryUrl = s;
		if(index<0){
			return;
		}
		this.baseQueryUrl = s.substring(0,index);
		int p = index+1;
		int length = s.length();
		while (p < length) {
			int p0 = p;
			while (p < length && s.charAt(p) != '=' && s.charAt(p) != '&' && s.charAt(p) != '?')
				p++;
			//String name = urlDecode(s.substring(p0, p));
			String name = s.substring(p0, p);
			if (p < length && s.charAt(p) == '=')
				p++;
			p0 = p;
			while (p < length && s.charAt(p) != '&' && s.charAt(p) != '?')
				p++;
			//String value = urlDecode(s.substring(p0, p));
			String value = s.substring(p0, p);
			if (p < length && (s.charAt(p) == '&' || s.charAt(p) == '?'))
				p++;
			
			ParamKeyVal param = new ParamKeyVal(name,value);
			this.paramList.add(param);
			List<ParamKeyVal> a = name2Params.get(name);
			if (a == null) {
				a = new ArrayList<ParamKeyVal>();
				name2Params.put(name, a);
			}
			a.add(param);
			List<ParamKeyVal> n = value2Params.get(value);
			if (n == null) {
				n = new ArrayList<ParamKeyVal>();
				value2Params.put(value, n);
			}
			n.add(param);
		}
	}

    /**
     * remove duplicated parameters, and keep the last one
     */
    public void removeDuplicated(){
        Iterator<Map.Entry<String, List<ParamKeyVal>>> iterator = name2Params.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<ParamKeyVal>> entry = iterator.next();
            List<ParamKeyVal> values = entry.getValue();
            if (values.size() > 1) {
                ArrayList<Object> removed = Lists.newArrayList();
                Set<Object> set = Sets.newHashSet();
                for (int i = 0; i < values.size(); i++) {
                    ParamKeyVal o = values.get(i);
                    if (set.contains(o.getValue())) {
                        removed.add(o);
                        this.paramList.remove(o);
                    }else {
                        set.add(o.getValue());
                    }
                }
                values.removeAll(removed);
            }
        }
    }

	@Override
	public String toString() {
		if(StringUtils.isEmpty(baseQueryUrl)){
			return "";
		}
		StringBuilder sb = new StringBuilder(baseQueryUrl);
		boolean first = true;
		for(ParamKeyVal param:paramList){
			if(first){
				sb.append("?");
				first = false;
			}else{
				sb.append("&");
			}
			sb.append(param.toString());
		}
		String str = sb.toString();
		return str.replaceAll(" ", "%20");
	}
	
	public static class ParamKeyVal{
		private String key;
		private String value;
		public String getKey() {
			return key;
		}
		public String getValue() {
			return value;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public ParamKeyVal(String key,String value){
			this.key = key;
			this.value = value;
		}
		
		public String toString() {
			return this.key+"="+this.value;
		}
	}
}
