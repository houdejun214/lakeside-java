package com.lakeside.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This Utils Class mainly used to handle some OS System Operation in linux, it is a supplementary to SystemUtils
 * 
 * @author houdejun
 *
 */
public class LinuxSystemUtils {

	public static final String OS_ARCH = getSystemProperty("os.arch");

	public static final String OS_NAME = getSystemProperty("os.name");

	public static final String OS_VERSION = getSystemProperty("os.version");

	public static final boolean IS_OS_LINUX = getOSMatchesName("Linux")  || getOSMatchesName("LINUX");
	
	private static final Pattern DISTRIB_ID_PATTERN = Pattern.compile("Distributor ID:\\s*(\\w*)\n");

	/**
	 * get the linux distributor name (or id)
	 * @return
	 */
	public static String getLinuxOSDistributionName() {
		if (IS_OS_LINUX) {
			String result = ShellCommand.execute("lsb_release -a");
			Matcher matcher = DISTRIB_ID_PATTERN.matcher(result);
			if(matcher.find()){
				String name = matcher.group(1);
				return name;
			}
		}
		return null;
	}

	private static boolean getOSMatchesName(String osNamePrefix) {
		if (OS_NAME == null) {
			return false;
		}
		return OS_NAME.startsWith(osNamePrefix);
	}

	private static String getSystemProperty(String property) {
		try {
			return System.getProperty(property);
		} catch (SecurityException ex) {
			// we are not allowed to look at this property
			System.err
					.println("Caught a SecurityException reading the system property '"
							+ property
							+ "'; the SystemUtils property value will default to null.");
			return null;
		}
	}
}
