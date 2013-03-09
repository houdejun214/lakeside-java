package com.lakeside.core.utils;

import java.util.List;
import java.util.Map;

public class QueryUrl {
	
	private String hostName;
	
	private Map<String,String[]> parameters ;
	
	private List<String> names;
	
	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public Map<String, String[]> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String[]> parameters) {
		this.parameters = parameters;
	}

	public List<String> getNames() {
		return names;
	}

	public void setNames(List<String> names) {
		this.names = names;
	}
	
	public void setParameter(String name,String value){
		parameters.put(name, new String[] { value });
		if(!names.contains(name)){
			names.add(name);
		}
	}
	
	public String[] getParameter(String name){
		String[] strings = parameters.get(name);
		return strings;
	}
	
	public String getOneParameter(String name){
		String[] strings = parameters.get(name);
		if(strings!=null&& strings.length>0){
			return strings[0];
		}
		return null;
	}

	public QueryUrl(String hostName,Map<String,String[]> parameters, List<String> names){
		this.hostName = hostName;
		this.parameters = parameters;
		this.names = names;
	}
	
	public QueryUrl(){
	}

	@Override
	public String toString() {
		if(StringUtils.isEmpty(hostName)){
			return "";
		}
		StringBuilder sb = new StringBuilder(hostName);
		boolean first = true;
		for(String name:names){
			if(first){
				sb.append("?");
				first = false;
			}else{
				sb.append("&");
			}
			String value = getOneParameter(name);
			sb.append(name).append("=").append(value);
		}
		String str = sb.toString();
		return str.replaceAll(" ", "%20");
	}
	
	public void removeParameter(String key){
		this.names.remove(key);
		this.parameters.remove(key);
	}
}
