package com.lakeside.core.json;

import org.json.simple.JSONObject;

public class JsonStatus {
	private String status;
	private String content;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public JsonStatus(){
		
	}
	
	public JsonStatus(String status,String content){
		this.status = status;
		this.content = content;
	}
	@Override
	public String toString() {
		JSONObject  obj =new JSONObject();
		obj.put("status", this.status);
		obj.put("content", this.content);
		return obj.toJSONString();
	}
}
