package com.lakeside.web.security;

import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.Maps;
import com.lakeside.core.utils.StringUtils;

/**
 * 此类是基于apache shiro 进行安全认证时，元数据定义的自动扫描实现。
 * @author houdejun
 *
 */
public class PermissionDefinitionSectionMetaSource extends AbstractMap<String,String>  implements  ApplicationContextAware,InitializingBean{
	
	private static final Map<String,String> EMPTY_SECTION = Maps.newHashMap();
	private static final Logger log = LoggerFactory.getLogger(PermissionDefinitionSectionMetaSource.class);
	
	private final Ini ini;
	
	public PermissionDefinitionSectionMetaSource() {
		ini = new Ini();
	}

    private String filterChainDefinitions;

	private ApplicationContext applicationContext;
	
	private PathMatcher pathMatcher = new AntPathMatcher();
	
	private boolean protectAll = true;
	
	private String defaultAccess = null;
	
	private Map<String,Protected> sectionUrls;
	
	public Map<String, Protected> getSectionUrls() {
		return sectionUrls;
	}
	/**
	 * default access rules
	 * @param defaultAccess
	 */
    public void setDefaultAccess(String defaultAccess) {
		this.defaultAccess = defaultAccess;
	}
    public boolean isProtectAll() {
		return protectAll;
	}

	public void setProtectAll(boolean protectAll) {
		this.protectAll = protectAll;
	}
	/**
     * 通过filterChainDefinitions对默认的url过滤定义
     * 
     * @param filterChainDefinitions 默认的url过滤定义
     */
    public void setFilterChainDefinitions(String filterChainDefinitions) {
        this.filterChainDefinitions = filterChainDefinitions;
    }

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void afterPropertiesSet() throws Exception {
		log.info("start detect url filter chain definitions.");
        //加载默认的url
        ini.load(filterChainDefinitions);
        Ini.Section section = ini.getSection(Ini.DEFAULT_SECTION_NAME);
        Map<String, Object> beansWithAnnotation = this.applicationContext.getBeansWithAnnotation(Controller.class);
        Iterator<Entry<String, Object>> iterator = beansWithAnnotation.entrySet().iterator();
        Map<String,Protected> urls = new LinkedHashMap<String,Protected>();
        while(iterator.hasNext()){
        	Entry<String, Object> next = iterator.next();
        	String beanName = next.getKey();
        	RequestMapping mapping = applicationContext.findAnnotationOnBean(beanName, RequestMapping.class);
        	Class<?> handlerType = applicationContext.getType(beanName);
        	Protected typeProtect = applicationContext.findAnnotationOnBean(beanName, Protected.class);
			Map<String, Protected> methodUrls = determineMethodsProtected(handlerType, mapping, typeProtect);
			urls.putAll(methodUrls);
			
			Boolean isControllerProtected = typeProtect!=null;
			boolean haveMappingSetting = (mapping!=null) && haveMappingSetting(mapping.value());
			if(isControllerProtected && haveMappingSetting){ 
				// controller 設置了protected 而且有Mapping設置
				for (String typeLevelPattern : mapping.value()) {
					if (!typeLevelPattern.startsWith("/")) {
						typeLevelPattern = "/" + typeLevelPattern;
					}
					if(!typeLevelPattern.endsWith("/")){
						typeLevelPattern = typeLevelPattern+"/";
					}
					urls.put(typeLevelPattern,typeProtect);
					urls.put(typeLevelPattern+"**",typeProtect);
				}
			}
        }
        for(String url:urls.keySet()){
          if(StringUtils.isNotEmpty(url) && !"/".equals(url)) {
        	  Protected protectAno = urls.get(url);
        	  StringBuilder auth = new StringBuilder("");
        	  if(protectAno!=null && protectAno.allowAnon()){
        		  auth.append("anon");
        	  }else if(protectAno!=null){
        		  auth.append("authc");
        		  String[] roles = protectAno.role();
        		  String[] permission = protectAno.permission();
        		  if(protectAno!=null && !isCollectionEmpty(roles)){
        			  auth.append(",roles[");
        			  for(int i=0;i<roles.length;i++){
        				  if(i>0){
        					  auth.append(",");
        				  }
        				  auth.append(roles[i]);
        			  }
        			  auth.append("]");
        		  }
        		  if(protectAno!=null && !isCollectionEmpty(permission)){
        			  auth.append(",perms[");
        			  for(int i=0;i<permission.length;i++){
        				  if(i>0){
        					  auth.append(",");
        				  }
        				  auth.append(permission[i]);
        			  }
        			  auth.append("]");
        		  }
        	  }
        	  if(protectAno == null){
        		  System.out.println();
        	  }
        	  log.info("detected url filter chain:"+url+"="+auth.toString());
        	  section.put(url,auth.toString());
          }
        }
        if(protectAll){
        	section.put("/**","authc");
        }else if(StringUtils.isNotEmpty(defaultAccess)){
        	section.put("/**",defaultAccess);
        }
        // cache the annotation information;
        this.sectionUrls = urls;
	}
    
	/**
     * 判断是否为空
     * @param list
     * @return
     */
    private boolean isCollectionEmpty(String[] list){
    	if(list!=null && list.length>0){
    		if(!StringUtils.isEmpty(list[0])){
    			return false;
    		}
    	}
    	return true;
    }
    
    private boolean haveMappingSetting(String[] typeLevelPatterns){
    	if(typeLevelPatterns!=null && typeLevelPatterns.length>0){
    		for (String typeLevelPattern : typeLevelPatterns) {
    			if(StringUtils.isNotEmpty(typeLevelPattern)){
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    /**
     * determine protected url patterns from methods
     * @param handlerType
     * @param typeLevelMapping
     * @param typeProtect
     * @return
     */
    protected Map<String, Protected> determineMethodsProtected(Class<?> handlerType, final RequestMapping typeLevelMapping,final Protected typeProtect) {
    	final Map<String,Protected> urls = new LinkedHashMap<String,Protected>();
		ReflectionUtils.doWithMethods(handlerType, new ReflectionUtils.MethodCallback() {
			public void doWith(Method method) {
				RequestMapping mapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
				Protected protect = AnnotationUtils.findAnnotation(method, Protected.class);
				if(protect == null){
					protect = typeProtect;
				}
				if(protect == null){
					// no protecting in this mapping
					return;
				}
				List<String> patterns = getMappingUrl(typeLevelMapping,mapping);
				if(patterns!=null){
					for(String pattern:patterns){
						addUrlsForPath(urls, pattern, protect);
					}
				}
			}
		}, ReflectionUtils.USER_DECLARED_METHODS);
		return urls;
	}
    
    /**
     * get the mapping urls from spring controller
     * 
     * @param typeLevelMapping
     * @param methodMapping
     * @return
     */
    private List<String> getMappingUrl(RequestMapping typeLevelMapping,RequestMapping methodMapping){
    	if(methodMapping==null || methodMapping.value()==null){
    		return null;
    	}
    	String[] typeLevelPatterns = null;
    	if(typeLevelMapping==null || typeLevelMapping.value()==null || typeLevelMapping.value().length==0){
    		typeLevelPatterns = new String[]{"/"};
    	}else{
    		typeLevelPatterns = typeLevelMapping.value();
    	}
    	
    	String[] methodLevelPatterns = methodMapping.value();
    	if(methodLevelPatterns==null || methodLevelPatterns.length==0){
    		methodLevelPatterns = new String[]{""};
    	}
    	List<String> patterns = new ArrayList<String>();
    	for(String typePattern:typeLevelPatterns){
    		if (!typePattern.startsWith("/")) {
    			typePattern = "/" + typePattern;
			}
    		for(String methodPattern:methodLevelPatterns){
    			String url = pathMatcher.combine(typePattern, methodPattern);
    			Matcher matcher = PATH_VARIABLE_TEMPLATE.matcher(url);
    			if(matcher.find()){
    				url = matcher.replaceAll("**");
    			}
    			patterns.add(getPathPattern(url));
    		}
    	}
    	return patterns;
    }
    
    private static final Pattern PATH_VARIABLE_TEMPLATE = Pattern.compile("\\{[^/]*\\}");
    private static final Pattern PATH_FIX_PATTERN = Pattern.compile("(\\\\|\\/)+");

	public static String getPathPattern(String path){
		if(StringUtils.isEmpty(path)){
			return "";
		}
		return PATH_FIX_PATTERN.matcher(path).replaceAll("/");
	}
    
    
    protected void addUrlsForPath(Map<String, Protected> urls, String path, Protected protect) {
		urls.put(path,protect);
	}
    
	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		Section section = ini.getSection(Ini.DEFAULT_SECTION_NAME);
		if(section!=null){
			return section.entrySet();
		}else{
			return EMPTY_SECTION.entrySet();
		}
	}

}