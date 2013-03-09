package com.lakeside.web.beans.config;

import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;
import org.springframework.util.StringValueResolver;

public class ConfigSettingStringValueResolver implements StringValueResolver,PlaceholderResolver {
	
	/** Default placeholder prefix: "${" */
	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

	/** Default placeholder suffix: "}" */
	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

	/** Default value separator: ":" */
	public static final String DEFAULT_VALUE_SEPARATOR = ":";
	
	public static final int SYSTEM_PROPERTIES_MODE_FALLBACK = 1;
	
	private String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;

	private String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;

	private String valueSeparator = DEFAULT_VALUE_SEPARATOR;

	private boolean ignoreUnresolvablePlaceholders = false;
	
	private ConfigSetting config;
	
	private PropertyPlaceholderHelper helper;
	
	public ConfigSettingStringValueResolver(ConfigSetting config){
		this.config = config;
		this.helper = new PropertyPlaceholderHelper(
				placeholderPrefix, placeholderSuffix, valueSeparator, ignoreUnresolvablePlaceholders);
	}

	public String resolveStringValue(String strVal) {
		String value = this.helper.replacePlaceholders(strVal, this);
		return value;
	}

	public String resolvePlaceholder(String placeholderName) {
		Object obj = this.config.getConfig(placeholderName);
		String val = null;
		if(obj!=null){
			val = obj.toString();
		}
		return val;
	}

}
