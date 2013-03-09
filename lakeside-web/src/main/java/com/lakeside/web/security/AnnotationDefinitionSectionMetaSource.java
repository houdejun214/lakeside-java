package com.lakeside.web.security;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
    
    public Section getObject() throws BeansException {
    	log.info("start detect url filter chain definitions.");
        Ini ini = new Ini();
        //加载默认的url
        ini.load(filterChainDefinitions);
        Ini.Section section = ini.getSection(Ini.DEFAULT_SECTION_NAME);
        Map<String, Object> beansWithAnnotation = this.applicationContext.getBeansWithAnnotation(Controller.class);
        Iterator<Entry<String, Object>> iterator = beansWithAnnotation.entrySet().iterator();
        Set<String> urls = new LinkedHashSet<String>();
        while(iterator.hasNext()){
        	Entry<String, Object> next = iterator.next();
        	String beanName = next.getKey();
        	RequestMapping mapping = applicationContext.findAnnotationOnBean(beanName, RequestMapping.class);
        	Class<?> handlerType = applicationContext.getType(beanName);
        	Boolean isControllerProtected = applicationContext.findAnnotationOnBean(beanName, Protected.class)!=null;
			if (mapping!=null && mapping.value().length > 0) {
				String[] typeLevelPatterns = mapping.value();
				boolean haveMappingSetting = haveMappingSetting(typeLevelPatterns);
				if(isControllerProtected && haveMappingSetting){ // controller 設置了protected 而且有Mapping設置
					for (String typeLevelPattern : typeLevelPatterns) {
						if (!typeLevelPattern.startsWith("/")) {
							typeLevelPattern = "/" + typeLevelPattern;
						}
						urls.add(typeLevelPattern);
					}
				}else{ // controller 沒有設置protected，或是設置了Protected但沒有具體的Mapping設置
					// @RequestMapping specifies paths at type level
					String[] methodLevelPatterns = determineUrlsForHandlerMethods(handlerType, true, isControllerProtected);
					for (String typeLevelPattern : typeLevelPatterns) {
						if (!typeLevelPattern.startsWith("/")) {
							typeLevelPattern = "/" + typeLevelPattern;
						}
						boolean hasEmptyMethodLevelMappings = false;
						for (String methodLevelPattern : methodLevelPatterns) {
							if (methodLevelPattern == null) {
								hasEmptyMethodLevelMappings = true;
							}
							else {
								String combinedPattern = pathMatcher.combine(typeLevelPattern, methodLevelPattern);
								addUrlsForPath(urls, combinedPattern);
							}
						}
						if (hasEmptyMethodLevelMappings ||
								org.springframework.web.servlet.mvc.Controller.class.isAssignableFrom(handlerType)) {
							addUrlsForPath(urls, typeLevelPattern);
						}
					}
				}
			}
			else {
				// actual paths specified by @RequestMapping at method level
				String[] methodUrls = determineUrlsForHandlerMethods(handlerType, false, isControllerProtected);
				for (String url : methodUrls) {
					urls.add(url);
				}
			}
        }
        for(String url:urls){
          if(StringUtils.isNotEmpty(url) && !"/".equals(url)) {
        	  if(!url.endsWith("**")){
        		  if(url.endsWith("/")){
        			  url = url.substring(0,url.length()-1);
        		  }
        		  log.info("detected url filter chain:"+url+"=authc");
            	  section.put(url,"authc");
            	  url +="/**";
            	  log.info("detected url filter chain:"+url+"=authc");
            	  section.put(url,"authc");
        	  }else{
	        	  log.info("detected url filter chain:"+url+"=authc");
	        	  section.put(url,"user");
        	  }
          }
        }
        return section;
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
    
    protected String[] determineUrlsForHandlerMethods(Class<?> handlerType, final boolean hasTypeLevelMapping,final boolean isControllerProtected) {
		final Set<String> urls = new LinkedHashSet<String>();
		ReflectionUtils.doWithMethods(handlerType, new ReflectionUtils.MethodCallback() {
			public void doWith(Method method) {
				RequestMapping mapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
				boolean isMethodProtected = AnnotationUtils.findAnnotation(method, Protected.class)!=null;
				if (mapping != null && (isControllerProtected||isMethodProtected)) {
					String[] mappedPatterns = mapping.value();
					if (mappedPatterns.length > 0) {
						for (String mappedPattern : mappedPatterns) {
							if (!hasTypeLevelMapping && !mappedPattern.startsWith("/")) {
								mappedPattern = "/" + mappedPattern;
							}
							addUrlsForPath(urls, mappedPattern);
						}
					}
					else if (hasTypeLevelMapping) {
						// empty method-level RequestMapping
						urls.add(null);
					}
				}
			}
		}, ReflectionUtils.USER_DECLARED_METHODS);
		return urls.toArray(new String[urls.size()]);
	}
    
    protected void addUrlsForPath(Set<String> urls, String path) {
		urls.add(path);
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
}