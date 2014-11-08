package com.lakeside.web.taglib;

import com.lakeside.core.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.Map;

public class ContentWebPager extends TagSupport{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String url;
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public int doEndTag() throws JspException {
		
		int page=getContextInt(WebPagerParam.PAGE_ATTRIBUTE,0);
		int pageCount=getContextInt(WebPagerParam.TOTAL_PAGE_ATTRIBUTE,0);
		if(pageCount<=1){
			return super.doEndTag();
		}
		int maxPage=getContextInt(WebPagerParam.MAX_PAGES,0);
		StringBuilder sb = new StringBuilder();
		if(page<=0)page=1;
		int start = 1;
		if((page-5)<=0){
			start = 1;
		}else{
			start = page-5;
		}
		int end = start+maxPage;
		if(end>pageCount){
			end = pageCount+1;
		}
		
		// start build the pager
		if(page>1){
			sb.append("<a id=\"pg-pre\" href=\""+getHref(page-1)+"\">&lt; Pre</a>");
		}
		for(int i=start;i<end;i++){
			int start_c=(i-1)*maxPage+1;
			int end_c=i*maxPage;
			if(i==page){
				sb.append("<strong class=\"cur\">"+i+"</strong> ");
			}else{
				sb.append("<a href=\""+getHref(i)+"\" title=\"Results "+start_c+" - "+end_c+"\">"+i+"</a> ");
			}
		}
		if(page<pageCount){
			sb.append("<a id=\"pg-next\" href=\""+getHref(page+1)+"\">Next &gt;</a></div>");
		}
		write(sb.toString());
		return super.doEndTag();
	}
	
	private int getContextInt(String name,int defaultValue){
		Object attribute = this.pageContext.getRequest().getAttribute(name);
		if(attribute==null){
			return defaultValue;
		}
		String value=attribute.toString();
		if(StringUtils.isNum(value)){
			return StringUtils.toInt(value);
		}
		return defaultValue;
	}
	
	private void write(String content){
		try {
			pageContext.getOut().write(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getHref(int page){
		HttpServletRequest request =(HttpServletRequest) this.pageContext.getRequest();
		String requestUrl = url;
		if(requestUrl==null || "".equals(requestUrl))
		{
			requestUrl = request.getAttribute("requesturl").toString();
		}
		Map<String, String> params = WebPagerParam.getParams(request);
		StringBuilder url = new StringBuilder(requestUrl);
		url.append("?");
		boolean containPageParam=false;
		for(String name:params.keySet()){
			if("page".equals(name) && !containPageParam){
				url.append("page="+page+"&");
				containPageParam=true;
			}else{
				url.append(""+name+"="+params.get(name)+"&");
			}
		}
		if(!containPageParam){
			url.append("page="+page);
		}
		String string = url.toString();
		return StringUtils.trimTrailingCharacter(string, '&');
	}
}
