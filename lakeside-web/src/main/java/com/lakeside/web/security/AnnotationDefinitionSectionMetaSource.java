package com.lakeside.web.security;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lakeside.core.utils.StringUtils;


public class AnnotationDefinitionSectionMetaSource implements FactoryBean<Ini.Section>,ApplicationContextAware{
	
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private String filterChainDefinitions;

	private ApplicationContext applicationContext;
	
	private PathMatcher pathMatcher = new AntPathMatcher();
	
	private boolean protectAll = true;
    
    public Section getObject() throws BeansException {
    	log.info("start detect url filter chain definitions.");
        Ini ini = new Ini();
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
			Boolean isControllerProtected = typeProtect!=null;
			if (mapping!=null) {
				String[] typeLevelPatterns = mapping.value();
				boolean haveMappingSetting = haveMappingSetting(typeLevelPatterns);
				
				// controller 沒有設置protected，或是設置了Protected但沒有具體的Mapping設置
				// @RequestMapping specifies paths at type level
				Map<String, Protected> methodLevelPatterns = determineMethodsProtected(
						handlerType, haveMappingSetting, isControllerProtected);
				if(typeLevelPatterns==null||typeLevelPatterns.length==0)typeLevelPatterns = new String[]{""};
				for (String typeLevelPattern : typeLevelPatterns) {
					if (!typeLevelPattern.startsWith("/")) {
						typeLevelPattern = "/" + typeLevelPattern;
					}
					//boolean hasEmptyMethodLevelMappings = false;
					for (String methodLevelPattern : methodLevelPatterns
							.keySet()) {
						Protected methodProtected = methodLevelPatterns.get(methodLevelPattern);
						if(methodProtected == null){
							methodProtected = typeProtect;
						}
						if (methodLevelPattern != null) {
							String combinedPattern = pathMatcher.combine(
									typeLevelPattern, methodLevelPattern);
							addUrlsForPath(urls, combinedPattern, methodProtected);
						}
					}
				}
				if(isControllerProtected && haveMappingSetting){ // controller 設置了protected 而且有Mapping設置
					for (String typeLevelPattern : typeLevelPatterns) {
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
			else {
				// actual paths specified by @RequestMapping at method level
				Map<String, Protected> methodUrls = determineMethodsProtected(handlerType, false, isControllerProtected);
				urls.putAll(methodUrls);
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
        }
        return section;
    }
    
    public boolean isProtectAll() {
		return protectAll;
	}

	public void setProtectAll(boolean protectAll) {
		this.protectAll = protectAll;
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
    
    protected Map<String, Protected> determineMethodsProtected(Class<?> handlerType, final boolean hasTypeLevelMapping,final boolean isControllerProtected) {
    	final Map<String,Protected> urls = new LinkedHashMap<String,Protected>();
		ReflectionUtils.doWithMethods(handlerType, new ReflectionUtils.MethodCallback() {
			public void doWith(Method method) {
				RequestMapping mapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
				Protected protect = AnnotationUtils.findAnnotation(method, Protected.class);
				boolean isMethodProtected = protect!=null;
				if (mapping != null && (isControllerProtected ||isMethodProtected)) {
					String[] mappedPatterns = mapping.value();
					if (mappedPatterns.length > 0) {
						for (String mappedPattern : mappedPatterns) {
							if (!hasTypeLevelMapping && !mappedPattern.startsWith("/")) {
								mappedPattern = "/" + mappedPattern;
							}
							addUrlsForPath(urls, mappedPattern,protect);
						}
					}
				}
			}
		}, ReflectionUtils.USER_DECLARED_METHODS);
		return urls;
	}
    
    protected void addUrlsForPath(Map<String, Protected> urls, String path, Protected protect) {
		urls.put(path,protect);
	}
    
    /**
     * 通过filterChainDefinitions对默认的url过滤定义
     * 
     * @param filterChainDefinitions 默认的url过滤定义
     */
    public void setFilterChainDefinitions(String filterChainDefinitions) {
        this.filterChainDefinitions = filterChainDefinitions;
    }

    
    public Class<?> getObjectType() {
        return this.getClass();
    }

    
    public boolean isSingleton() {
        return false;
    }

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
//	
//	private static class UrlMap extends HashMap<String,Protected>{
//
//		private List<String> urls = new ArrayList<String>();
//		
//		@Override
//		public Protected put(String key, Protected value) {
//			boolean containKey = this.containsKey(key);
//			if(!containKey){
//				urls.add(key);
//			}
//			return super.put(key, value);
//		}
//
//		@Override
//		public Protected remove(Object key) {
//			urls.remove(key);
//			return super.remove(key);
//		}
//		
//	}
}