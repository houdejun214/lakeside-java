package com.lakeside.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lakeside.core.utils.QueryUrl.ParamKeyVal;

/**
 * url 格式化处理器
 * 
 * http://www.google.com/?q={query}&m=abc&loc={loc}&n=12
 * 
 * 对这样的url中包含的格式化参数进行解析并能根据参数情况进行自动的参数填充
 * 
 * @author hdj
 *
 */
public class UrlFormater {
	private final QueryUrl url;
	/**
	 * 格式化参数名称list
	 */
	private final List<String> formatNameList;
	
	/**
	 * 格式化参数名称和原值的对应关系,值记录参数（baseurl中的不记录）
	 */
	private final Map<String,List<String>> formatParamValues;
	
	private boolean findInBaseUrl = false;
	private static final Pattern PATTERN = Pattern.compile("\\{([a-zA-Z0-9]*)\\}");
	private String baseQueryUrl;
	public UrlFormater(String s){
		url = new QueryUrl(s);
		baseQueryUrl = url.getBaseQueryUrl();
		formatNameList = new ArrayList<String>();
		formatParamValues = new HashMap<String,List<String>>();
		Matcher matcher = PATTERN.matcher(baseQueryUrl);
		while(matcher.find()){
			String formatName = matcher.group(1);
			if(!formatNameList.contains(formatName)){
				formatNameList.add(formatName);
			}
			findInBaseUrl = true;
		}
		Set<String> values = url.getValues();
		for(String val:values){
			matcher = PATTERN.matcher(val);
			while(matcher.find()){
				String formatName = matcher.group(1);
				List<String> list = formatParamValues.get(formatName);
				if(list==null){
					list = new ArrayList<String>();
					formatParamValues.put(formatName, list);
				}
				list.add(val);
				if(!formatNameList.contains(formatName)){
					formatNameList.add(formatName);
				}
			}
		}
	}
	
	public List<String> getFormatNameList(){
		return this.formatNameList;
	}
	
	public String format(Map<String,?> params){
		if(params == null){
			params = new HashMap<String, Object>();
		}
		for(String formateName:formatNameList){
			List<String> list = formatParamValues.get(formateName);
			if(params.containsKey(formateName)){
				String _fparam = StringUtils.valueOf(params.get(formateName));
				if(list!=null){
					for(String origiParamValue:list){
						List<ParamKeyVal> parameters = url.getParameterByValue(origiParamValue);
						if(parameters!=null){
							for(ParamKeyVal param:parameters){
								String value = param.getValue();
								String newValue = value.replaceAll("\\{"+formateName+"\\}", _fparam);
								param.setValue(newValue);
							}
						}
					}
				}
				if(findInBaseUrl){
					this.baseQueryUrl = this.baseQueryUrl.replaceAll("\\{"+formateName+"\\}", _fparam);
				}
			}else{
				// 没有指定对于的匹配值，但包含在baseurl中
				if(findInBaseUrl){
					throw new RuntimeException("["+formateName+"] found in the base url, must be specified.");
				}
				// 参数中没有制定格式化规则，需要去除该参数
				for(String param:list){
					url.removeParameterByValue(param);
				}
			}
		}
		this.url.setBaseQueryUrl(baseQueryUrl);
		return url.toString();
	}
}
