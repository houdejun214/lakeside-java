
package com.lakeside.http.robots;

import java.util.StringTokenizer;


public class RobotstxtParser {

	private static final String PATTERNS_USERAGENT = "(?i)^User-agent:.*";
	private static final String PATTERNS_DISALLOW = "(?i)Disallow:.*";
	private static final String PATTERNS_ALLOW = "(?i)Allow:.*";
	
	private static final int PATTERNS_USERAGENT_LENGTH = 11;
	private static final int PATTERNS_DISALLOW_LENGTH = 9;
	private static final int PATTERNS_ALLOW_LENGTH = 6;
	
	public static HostDirectives parse(String content, String myUserAgent) {
		
		HostDirectives directives = null;
		boolean inMatchingUserAgent = false;		
		
		StringTokenizer st = new StringTokenizer(content, "\n");
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			
			int commentIndex = line.indexOf("#");
			if (commentIndex > -1) {				
				line = line.substring(0, commentIndex);
			}

			// remove any html markup
			line = line.replaceAll("<[^>]+>", "");

			line = line.trim();

			if (line.length() == 0) {
				continue;
			}

			if (line.matches(PATTERNS_USERAGENT)) {
				String ua = line.substring(PATTERNS_USERAGENT_LENGTH).trim().toLowerCase();
				if (ua.equals("*") || ua.contains(myUserAgent)) {
					inMatchingUserAgent = true;
					if (directives == null) {
						directives = new HostDirectives();
					}
				} else {
					inMatchingUserAgent = false;
				}
			} else if (line.matches(PATTERNS_DISALLOW)) {
				if (!inMatchingUserAgent) {
					continue;
				}
				String path = line.substring(PATTERNS_DISALLOW_LENGTH).trim();
				if (path.endsWith("*")) {
					path = path.substring(0, path.length() - 1);
				}
				path = path.trim();
				if (path.length() > 0) {
					directives.addDisallow(path);	
				}								
			} else if (line.matches(PATTERNS_ALLOW)) {
				if (!inMatchingUserAgent) {
					continue;
				}
				String path = line.substring(PATTERNS_ALLOW_LENGTH).trim();
				if (path.endsWith("*")) {
					path = path.substring(0, path.length() - 1);
				}
				path = path.trim();
				directives.addAllow(path);
			}			
		}
		
		return directives;
	}
}
