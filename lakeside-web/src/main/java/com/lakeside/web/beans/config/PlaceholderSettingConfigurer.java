package com.lakeside.web.beans.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.StringValueResolver;

public class PlaceholderSettingConfigurer implements BeanFactoryPostProcessor, PriorityOrdered,BeanNameAware, BeanFactoryAware  
{
	
	public PlaceholderSettingConfigurer(){
	}
	
	private String beanName;

	private BeanFactory beanFactory;

	private int order = Ordered.LOWEST_PRECEDENCE;  // default: same as non-Ordered
	
	private ConfigSetting config;

	public void setOrder(int order) {
	  this.order = order;
	}

	public int getOrder() {
	  return this.order;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if(this.config==null){
			this.config = this.beanFactory.getBean(ConfigSetting.class);
		}
		StringValueResolver valueResolver = new ConfigSettingStringValueResolver(config);
		BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(valueResolver);

		String[] beanNames = beanFactory.getBeanDefinitionNames();
		for (String curName : beanNames) {
			if (!(curName.equals(this.beanName) && beanFactory.equals(this.beanFactory))) {
				BeanDefinition bd = beanFactory.getBeanDefinition(curName);
				try {
					visitor.visitBeanDefinition(bd);
				}
				catch (Exception ex) {
					throw new BeanDefinitionStoreException(bd.getResourceDescription(), curName, ex.getMessage());
				}
			}
		}
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public void setBeanName(String name) {
		this.beanName = name;
	}
}
