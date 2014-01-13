/*
 * (./) PatternUtils.java
 * 
 * (cc) copyright@2010-2011
 * 
 * 
 * this library is all rights reserved , but you can used it for free.
 * if you want more support or functions, please contact with us!
 */
package com.lakeside.core.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Auto-generated Javadoc
/**
 * The Class PatternUtils.
 *
 * @author Hou Dejun
 */
public class PatternUtils {
	
	/** The Constant patterns. */
	private static final Map<String, Pattern> patterns = new HashMap<String, Pattern>();

	/**
	 * Gets the pattern.
	 *
	 * @param patternStr the pattern str
	 * @return the pattern
	 */
	public static Pattern getPattern(String patternStr) {
		return getPattern(patternStr,0);
	}
	
	/**
	 * Gets the pattern.
	 * @param  flags
     *         Match flags, a bit mask that may include
     *         {@link #CASE_INSENSITIVE}, {@link #MULTILINE}, {@link #DOTALL},
     *         {@link #UNICODE_CASE}, {@link #CANON_EQ}, {@link #UNIX_LINES},
     *         {@link #LITERAL}, {@link #UNICODE_CHARACTER_CLASS}
     *         and {@link #COMMENTS}
	 *
	 * @param patternStr the pattern str
	 * @return the pattern
	 */
	public static Pattern getPattern(String patternStr,int flags) {
		if (patterns.containsKey(patternStr)) {
			return patterns.get(patternStr);
		}
		Pattern pattern = Pattern.compile(patternStr,flags);
		patterns.put(patternStr, pattern);
		return pattern;
	}

	/**
	 * Matches.
	 *
	 * @param patternStr the pattern str
	 * @param input the input
	 * @return true, if successful
	 */
	public static boolean matches(String patternStr, String input) {
		return getPattern(patternStr).matcher(input).matches();
	}
	
	/**
	 * find in a input string with a regex pattern String
	 * 
	 * @param patternStr
	 * @param input
	 * @return
	 */
	public static boolean find(String patternStr, String input) {
		return getPattern(patternStr).matcher(input).find();
	}

	
	public static String getMatchPattern(String regex,String input,int groupIndex){
		return getMatchPattern(regex,input,groupIndex,0);
	}
	
	public static String getMatchPattern(String regex,String input,int groupIndex,int flags){
		Pattern pat = getPattern(regex,flags);
		Matcher matcher = pat.matcher(input);
		if(matcher.find()){
			return matcher.group(groupIndex);
		}
		return "";
	}
	
	public static String replaceMatchGroup(String regex,String input,int groupIndex,String replacestr){
		StringBuilder sb = new StringBuilder();
		Pattern pat = getPattern(regex);
		Matcher matcher = pat.matcher(input);
		if(matcher.find()){
			 int start = matcher.start(groupIndex);
			 int end = matcher.end(groupIndex);
			 sb.append(input.substring(0, start));
			 sb.append(replacestr);
			 sb.append(input.substring(end));
			 return sb.toString();
		}
		return "";
	}
	
	
	public static String[] getMatchPattern(String regex,String input){
		Pattern pat = getPattern(regex);
		Matcher matcher = pat.matcher(input);
		if(matcher.find()){
			String[] results= new String[matcher.groupCount()];
			for(int i=0;i<matcher.groupCount();i++){
				results[i]=matcher.group(i+1);
			}
			return results;
		}
		return null;
	}
	
	public static String trimMatch(String regex,String input){
		Pattern pat = getPattern(regex);
		Matcher matcher = pat.matcher(input);
		String text = matcher.replaceAll("");
		return text;
	}
}
