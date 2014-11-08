package com.lakeside.web.taglib;

import com.lakeside.core.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class WebPagerParam {
	
	private static String ModifiableParametersAttr = "modifiableParameters";
	public static final String PAGE_ATTRIBUTE="page";
	public static final String TOTAL_PAGE_ATTRIBUTE="totalPage";
	public static final String TOTAL_COUNT_ATTRIBUTE="totalCount";
	public static final String MAX_PAGES="maxpages";

	private int page=0;
	
	private int totalPage=0;
	
	private int totalCount=0;
	
	private int countOfPage = 0;
	
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		if(page<=0){
			page=0;
		}
		this.page = page;
		this.request.setAttribute(PAGE_ATTRIBUTE,page);
	}
	public int getTotalPage() {
		return totalPage;
	}
	public int getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount=totalCount;
		this.totalPage = this.totalCount/countOfPage;
		if(this.totalCount%countOfPage!=0){
			this.totalPage++;
		}
		this.request.setAttribute(TOTAL_PAGE_ATTRIBUTE,totalPage);
		this.request.setAttribute(TOTAL_COUNT_ATTRIBUTE , NumberFormat.getNumberInstance().format(this.totalCount));
	}
	public int getCountOfPage() {
		return countOfPage;
	}
	public void setCountOfPage(int countOfPage) {
		this.countOfPage = countOfPage;
	}

	private Map<String,String> params = new HashMap<String,String>();
	
	private final HttpServletRequest request;

	
	public WebPagerParam(HttpServletRequest request) throws UnsupportedEncodingException{
		this.request = request;
		this.initPageParams();
		Enumeration parameterNames = request.getParameterNames();
		while(parameterNames.hasMoreElements()) {
			String name = parameterNames.nextElement().toString();
			String param = new String(request.getParameter(name).getBytes("ISO-8859-1"), "UTF-8");
			params.put(name, param);
		}
	}
	
	private void initPageParams(){
		String _page = this.request.getParameter("page");
		if(!StringUtils.isEmpty(_page)){
			page = StringUtils.toInt(_page);
		}
		this.request.setAttribute("page", page);
	}
	
	public String get(String name){
		return this.params.get(name);
	}
	
	public void setAttribute(String name,Object val){
		this.request.setAttribute(name, val);
	}
	
	public Object getAttribute(String name){
		return this.request.getAttribute(name);
	}
	
	public void addParams(String name ,Object val){
		
		Map<String,String> parameters =(Map<String,String> )this.request.getAttribute(ModifiableParametersAttr);
		if(parameters==null){
			parameters = new HashMap<String,String>();
			this.request.setAttribute(ModifiableParametersAttr, parameters);
		}
		String valStr="";
		if(val!=null)valStr=val.toString();
		parameters.put(name, valStr);
	}
	
	public static Map<String,String> getParams(HttpServletRequest request){
		Enumeration parameterNames = request.getParameterNames();
		Map<String,String> params = new HashMap<String,String>();
		while(parameterNames.hasMoreElements()) {
			String name = parameterNames.nextElement().toString();
			String parameter = request.getParameter(name);
			if(parameter!=null && !"".equals(parameter)){
				params.put(name, parameter);
			}
		}
		Map<String,String> modifParams =(Map<String,String> )request.getAttribute(ModifiableParametersAttr);
		if(modifParams!=null){
			params.putAll(modifParams);
		}
		return params;
	}
	
	public String getLanguage(){
		return this.request.getParameter("lang");
	}
}
