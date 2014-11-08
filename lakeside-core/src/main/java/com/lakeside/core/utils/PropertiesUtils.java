/*
 * $HeadURL: https://springside.googlecode.com/svn/springside3/trunk/modules/core/src/main/java/org/springside/modules/utils/PropertiesUtils.java $
 * $Id: PropertiesUtils.java 1097 2010-05-22 13:11:45Z calvinxiu $
 * Copyright (c) 2010 by Ericsson, all rights reserved.
 */

package com.lakeside.core.utils;

import com.lakeside.core.resource.DefaultPropertiesPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Properties Util函数.
 */
public class PropertiesUtils {

	private static final String DEFAULT_ENCODING = "UTF-8";

	private static Logger logger = LoggerFactory.getLogger(PropertiesUtils.class);

	private static DefaultPropertiesPersister propertiesPersister = new DefaultPropertiesPersister();
	
	/**
	 * 载入多个properties文件, 相同的属性最后载入的文件将会覆盖之前的载入.
	 * @see org.springframework.beans.factory.config.PropertyPlaceholderConfigurer
	 */
	public static Properties loadProperties(String... locations) throws IOException {
		return loadProperties(PropertiesUtils.class,locations);
	}
	
	public static Properties loadProperties(Class<?> classresource,String... locations) throws IOException{
		ClassLoader classLoader = classresource.getClassLoader();
		Properties props = new Properties();
		for (String location : locations) {

			logger.debug("Loading properties file from classpath:" + location);

			InputStream is = null;
			try {
				is = classLoader.getResourceAsStream(location);
				propertiesPersister.load(props, new InputStreamReader(is, DEFAULT_ENCODING));
			} catch (IOException ex) {
				logger.info("Could not load properties from classpath:" + location + ": " + ex.getMessage());
			} finally {
				if (is != null) {
					is.close();
				}
			}
		}
		return props;
	}
}
