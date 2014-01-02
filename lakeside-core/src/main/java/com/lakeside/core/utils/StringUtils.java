/*
 * (./) StringUtils.java
 * 
 * (cc) copyright@2010-2011
 * 
 * 
 * this library is all rights reserved , but you can used it for free.
 * if you want more support or functions, please contact with us!
 */
package com.lakeside.core.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;

import com.lakeside.core.regex.CommonPattern;

/**
 * String procesing tools class.
 * 
 * @author Dejun Hou
 */
public class StringUtils {
	
	public static final int INDEX_NOT_FOUND = -1;

	/**
	 * Check that the given CharSequence is neither <code>null</code> nor of length 0.
	 * Note: Will return <code>true</code> for a CharSequence that purely consists of whitespace.
	 * <p><pre>
	 * StringUtils.hasLength(null) = false
	 * StringUtils.hasLength("") = false
	 * StringUtils.hasLength(" ") = true
	 * StringUtils.hasLength("Hello") = true
	 * </pre>
	 * @param str the CharSequence to check (may be <code>null</code>)
	 * @return <code>true</code> if the CharSequence is not null and has length
	 * @see #hasText(String)
	 */
	public static boolean hasLength(CharSequence str) {
		return (str != null && str.length() > 0);
	}

	/**
	 * Check that the given String is neither <code>null</code> nor of length 0.
	 * Note: Will return <code>true</code> for a String that purely consists of whitespace.
	 * @param str the String to check (may be <code>null</code>)
	 * @return <code>true</code> if the String is not null and has length
	 * @see #hasLength(CharSequence)
	 */
	public static boolean hasLength(String str) {
		return hasLength((CharSequence) str);
	}

	/**
	 * Check whether the given CharSequence has actual text.
	 * More specifically, returns <code>true</code> if the string not <code>null</code>,
	 * its length is greater than 0, and it contains at least one non-whitespace character.
	 * <p><pre>
	 * StringUtils.hasText(null) = false
	 * StringUtils.hasText("") = false
	 * StringUtils.hasText(" ") = false
	 * StringUtils.hasText("12345") = true
	 * StringUtils.hasText(" 12345 ") = true
	 * </pre>
	 * @param str the CharSequence to check (may be <code>null</code>)
	 * @return <code>true</code> if the CharSequence is not <code>null</code>,
	 * its length is greater than 0, and it does not contain whitespace only
	 * @see java.lang.Character#isWhitespace
	 */
	public static boolean hasText(CharSequence str) {
		if (!hasLength(str)) {
			return false;
		}
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether the given String has actual text.
	 * More specifically, returns <code>true</code> if the string not <code>null</code>,
	 * its length is greater than 0, and it contains at least one non-whitespace character.
	 * @param str the String to check (may be <code>null</code>)
	 * @return <code>true</code> if the String is not <code>null</code>, its length is
	 * greater than 0, and it does not contain whitespace only
	 * @see #hasText(CharSequence)
	 */
	public static boolean hasText(String str) {
		return hasText((CharSequence) str);
	}

	/**
	 * check whether a string is empty.
	 * 
	 * @param str
	 *            the str
	 * @return the boolean
	 */
	public static Boolean isEmpty(String str) {
		if (str == null || str.length() == 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * check whether a string is not empty
	 * @param str
	 * @return
	 */
	public static Boolean isNotEmpty(String str){
		if (str == null || str.length() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * check whether if it is a number string.
	 * 
	 * @param s
	 *            the string
	 * @return check result
	 */
	public static boolean isNum(final String s) {
		if (isEmpty(s)) {
			return false;
		}
		return PatternUtils.matches(CommonPattern.NUMBER, s);
	}

	/**
	 * check whether if it is a integer string.
	 * 
	 * @param s
	 *            the s
	 * @return check result
	 */
	public static boolean isInt(final String s) {
		if (isEmpty(s)) {
			return false;
		}
		return PatternUtils.matches(CommonPattern.INT, s);
	}

	/**
	 * parser a string to an integer type .
	 * 
	 * @param s
	 *            需要转换的字符串
	 * @return 符合数字格式返回对应的数字 不符合则返回0
	 */
	public static int toInt(final String s) {
		if (isInt(s)) {
			return Integer.parseInt(s);
		}
		return 0;
	}
	
	/**
	 * parser a string a long type
	 * @param s
	 * @return
	 */
	public static long toLong(final String s) {
		if (isInt(s)) {
			return Long.valueOf(s);
		}
		return 0;
	}

	/**
	 * parser a string to an double type .
	 * 
	 * @param s
	 *            需要转换的字符串
	 * @return 符合数字格式返回对应的数字 不符合则返回0
	 */
	public static double toDouble(final String s) {
		if (!isNum(s)) {
			return 0;
		}
		return Double.parseDouble(s);
	}

	private static Map<String,MessageFormat> formats=new HashMap<String,MessageFormat>();
	
	/**
	 * format the given pattern with given arguments
	 * 
	 * @param pattern
	 *            pattern string
	 * @param args
	 *            format arguments
	 * @return
	 */
	public static String format(String pattern, Object... args) {
		MessageFormat temp = formats.get(pattern);
		if(temp==null){
		  temp = new MessageFormat(pattern);
		  formats.put(pattern, temp);
		}
		return temp.format(pattern, args);
	}
	/**
	 * format a long, with zero padding at head
	 * @param n
	 * @param digits
	 * @return
	 */
	public static String formatLong(long n, int digits) {
	    /*
	          we create a format :
	           %% : %  the first % is to escape the second %
	           0  : 0  zero character
	           %d :    how many '0' we want (specified by digits)
	           d  : d  the number to format
	
	    */
	    String format = String.format("%%0%dd", digits);
	    return String.format(format, n);
	}
	
	/**
	 * format a long, with zero padding at head
	 * @param n
	 * @param digits
	 * @return
	 */
	public static String formatInt(int n, int digits) {
	    /*
	          we create a format :
	           %% : %  the first % is to escape the second %
	           0  : 0  zero character
	           %d :    how many '0' we want (specified by digits)
	           d  : d  the number to format
	
	    */
	    String format = String.format("%%0%dd", digits);
	    return String.format(format, n);
	}

	/**
	 * get a string object from native char array
	 * 
	 * @param chars
	 * @return
	 */
	public static String getString(byte[] chars) {
		if (chars == null || chars.length < 1)
			throw new IllegalArgumentException(
					"this byteArray must not be null or empty");
		final StringBuilder string = new StringBuilder();
		for (int i = 0; i < chars.length; i++) {
			if (!Character.isDefined(chars[i]) || chars[i] == 0) {
				break;
			}
			char nowchar = (char) (chars[i] & 0xFF);
			string.append(nowchar);
		}
		return string.toString();

	}

	/**
	 * <p>
	 * Removes control characters, including whitespace, from both ends of this
	 * String, handling <code>null</code> by returning <code>null</code>.
	 * </p>
	 * 
	 * @see java.lang.String#trim()
	 * @param str
	 *            the String to check
	 * @return the trimmed text (or <code>null</code>)
	 */
	public static String trim(String str) {
		return (str == null ? null : str.trim());
	}

	/**
	 * <p>
	 * Deletes all whitespaces from a String.
	 * </p>
	 * 
	 * <p>
	 * Whitespace is defined by {@link Character#isWhitespace(char)}.
	 * </p>
	 * 
	 * @param str
	 *            String target to delete whitespace from
	 * @return the String without whitespaces
	 * @throws NullPointerException
	 */
	public static String deleteWhitespace(String str) {
		StringBuffer buffer = new StringBuffer();
		int sz = str.length();
		for (int i = 0; i < sz; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				buffer.append(str.charAt(i));
			}
		}
		return buffer.toString();
	}

	/**
	 * <p>
	 * Remove a newline if and only if it is at the end of the supplied String.
	 * </p>
	 * 
	 * @param str
	 *            String to chomp from
	 * @return String without chomped ending
	 * @throws NullPointerException
	 *             if str is <code>null</code>
	 */
	public static String chompLast(String str) {
		return chompLast(str, "\n");
	}

	/**
	 * <p>
	 * Remove a value if and only if the String ends with that value.
	 * </p>
	 * 
	 * @param str
	 *            String to chomp from
	 * @param sep
	 *            String to chomp
	 * @return String without chomped ending
	 * @throws NullPointerException
	 *             if str or sep is <code>null</code>
	 */
	public static String chompLast(String str, String sep) {
		if (str.length() == 0) {
			return str;
		}
		String sub = str.substring(str.length() - sep.length());
		if (sep.equals(sub)) {
			return str.substring(0, str.length() - sep.length());
		} else {
			return str;
		}
	}

	/**
	 * <p>
	 * Remove everything and return the last value of a supplied String, and
	 * everything after it from a String.
	 * </p>
	 * 
	 * @param str
	 *            String to chomp from
	 * @param sep
	 *            String to chomp
	 * @return String chomped
	 * @throws NullPointerException
	 *             if str or sep is <code>null</code>
	 */
	public static String getChomp(String str, String sep) {
		int idx = str.lastIndexOf(sep);
		if (idx == str.length() - sep.length()) {
			return sep;
		} else if (idx != -1) {
			return str.substring(idx);
		} else {
			return "";
		}
	}

	/*
	 * <p>Remove the last newline, and everything after it from a String.</p>
	 * 
	 * @param str String to chomp the newline from
	 * 
	 * @return String without chomped newline
	 * 
	 * @throws NullPointerException if str is <code>null</code>
	 */
	public static String chomp(String str) {
		return chomp(str, "\n");
	}

	/**
	 * <p>
	 * Remove the last value of a supplied String, and everything after it from
	 * a String.
	 * </p>
	 * 
	 * @param str
	 *            String to chomp from
	 * @param sep
	 *            String to chomp
	 * @return String without chomped ending
	 * @throws NullPointerException
	 *             if str or sep is <code>null</code>
	 */
	public static String chomp(String str, String sep) {
		int idx = str.lastIndexOf(sep);
		if (idx != -1) {
			return str.substring(0, idx);
		} else {
			return str;
		}
	}
	
	public static String chompHeader(String str,String prex){
		if(str==null){
			return "";
		}
		if(str.startsWith(prex)){
			return str.substring(prex.length());
		}
		return str;
	}

	/**
	 * <p>
	 * How many times is the substring in the larger String.
	 * </p>
	 * 
	 * <p>
	 * <code>null</code> returns <code>0</code>.
	 * </p>
	 * 
	 * @param str
	 *            the String to check
	 * @param sub
	 *            the substring to count
	 * @return the number of occurances, 0 if the String is <code>null</code>
	 * @throws NullPointerException
	 *             if sub is <code>null</code>
	 */
	public static int countMatches(String str, String sub) {
		if (sub.equals("")) {
			return 0;
		}
		if (str == null) {
			return 0;
		}
		int count = 0;
		int idx = 0;
		while ((idx = str.indexOf(sub, idx)) != -1) {
			count++;
			idx += sub.length();
		}
		return count;
	}

	/**
	 * <p>
	 * Remove the last character from a String.
	 * </p>
	 * 
	 * <p>
	 * If the String ends in <code>\r\n</code>, then remove both of them.
	 * </p>
	 * 
	 * @param str
	 *            String to chop last character from
	 * @return String without last character
	 * @throws NullPointerException
	 *             if str is <code>null</code>
	 */
	public static String chop(String str) {
		if ("".equals(str)) {
			return "";
		}
		if (str.length() == 1) {
			return "";
		}
		int lastIdx = str.length() - 1;
		String ret = str.substring(0, lastIdx);
		char last = str.charAt(lastIdx);
		if (last == '\n') {
			if (ret.charAt(lastIdx - 1) == '\r') {
				return ret.substring(0, lastIdx - 1);
			}
		}
		return ret;
	}

	public static String padLeft(String str_org, int length, char padchar) {
		if (isEmpty(str_org)) {
			return "";
		}
		if (str_org.length() >= length) {
			return str_org;
		}
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < length - str_org.length(); i++) {
			str.append(padchar);
		}
		str.append(str_org);
		return str.toString();
	}
	
	public static int byteLenth(String str){
		if (isEmpty(str))
			return 0;
		return str.getBytes().length;
	}

	public static String splitString(String str, int length)
	{
		StringBuilder reStr = new StringBuilder();
		if (isEmpty(str))
			return "";
		char[] tempChar = str.toCharArray();
		int count=0;
		for (int i = 0; (i < tempChar.length && length > count); i++) {
			char c = tempChar[i];
			String str_char = String.valueOf(c);
			int charByteLen = str_char.getBytes().length;
			count+=charByteLen;
			if(count<=length){
				reStr.append(c);
			}
		}
		return reStr.toString();
	}
	
	public static String trim(String str,String chars){
		if(str==null || "".equals(str)){
			return "";
		}
		str = str.replaceFirst("^["+chars+"]*", "");
		str = str.replaceFirst("["+chars+"]*$", "");
		return str;
	}
	

	/**
	 * Trim all occurences of the supplied leading character from the given String.
	 * @param str the String to check
	 * @param leadingCharacter the leading character to be trimmed
	 * @return the trimmed String
	 */
	public static String trimLeadingCharacter(String str, char leadingCharacter) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && sb.charAt(0) == leadingCharacter) {
			sb.deleteCharAt(0);
		}
		return sb.toString();
	}

	/**
	 * Trim all occurences of the supplied trailing character from the given String.
	 * @param str the String to check
	 * @param trailingCharacter the trailing character to be trimmed
	 * @return the trimmed String
	 */
	public static String trimTrailingCharacter(String str, char trailingCharacter) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && sb.charAt(sb.length() - 1) == trailingCharacter) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	
	/**
	 * Trim leading whitespace from the given String.
	 * @param str the String to check
	 * @return the trimmed String
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimLeadingWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
			sb.deleteCharAt(0);
		}
		return sb.toString();
	}
	
	/**
	 * Trim trailing whitespace from the given String.
	 * @param str the String to check
	 * @return the trimmed String
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimTrailingWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
	
	 public static String replace(String text, String searchString, String replacement) {
        return replace(text, searchString, replacement, -1);
	 }

    /**
     * <p>Replaces a String with another String inside a larger String,
     * for the first <code>max</code> values of the search String.</p>
     *
     * <p>A <code>null</code> reference passed to this method is a no-op.</p>
     *
     * <pre>
     * StringUtils.replace(null, *, *, *)         = null
     * StringUtils.replace("", *, *, *)           = ""
     * StringUtils.replace("any", null, *, *)     = "any"
     * StringUtils.replace("any", *, null, *)     = "any"
     * StringUtils.replace("any", "", *, *)       = "any"
     * StringUtils.replace("any", *, *, 0)        = "any"
     * StringUtils.replace("abaa", "a", null, -1) = "abaa"
     * StringUtils.replace("abaa", "a", "", -1)   = "b"
     * StringUtils.replace("abaa", "a", "z", 0)   = "abaa"
     * StringUtils.replace("abaa", "a", "z", 1)   = "zbaa"
     * StringUtils.replace("abaa", "a", "z", 2)   = "zbza"
     * StringUtils.replace("abaa", "a", "z", -1)  = "zbzz"
     * </pre>
     *
     * @param text  text to search and replace in, may be null
     * @param searchString  the String to search for, may be null
     * @param replacement  the String to replace it with, may be null
     * @param max  maximum number of values to replace, or <code>-1</code> if no maximum
     * @return the text with any replacements processed,
     *  <code>null</code> if null String input
     */
    public static String replace(String text, String searchString, String replacement, int max) {
        if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
            return text;
        }
        int start = 0;
        int end = text.indexOf(searchString, start);
        if (end == INDEX_NOT_FOUND) {
            return text;
        }
        int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        increase = (increase < 0 ? 0 : increase);
        increase *= (max < 0 ? 16 : (max > 64 ? 64 : max));
        StringBuilder buf = new StringBuilder(text.length() + increase);
        while (end != INDEX_NOT_FOUND) {
            buf.append(text.substring(start, end)).append(replacement);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = text.indexOf(searchString, start);
        }
        buf.append(text.substring(start));
        return buf.toString();
    }
    
	/**
	 * MD5加密
	 * @param strPlain
	 * @return
	 * @throws Exception
	 */
	public static String md5Encode(String strPlain){
		byte[] s = md5(strPlain);
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < s.length; i++) {
			result.append(Integer.toHexString((0x000000ff & s[i]) | 0xffffff00).substring(6));
		}
		return result.toString();
	}
	
	/**
	 * 获取MD5 16位字节信息
	 * @param strPlain
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] md5(String strPlain){
		byte s[] = null;
		try {
			MessageDigest m= MessageDigest.getInstance("MD5");
			m.update(strPlain.getBytes("UTF8"));
			s = m.digest();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return s;
	}
	
	/**
	 * SHA-1 加密
	 * @param strPlain
	 * @return
	 * @throws Exception
	 */
	public static String shaEncode(String strPlain){
		byte[] s = sha(strPlain);
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < s.length; i++) {
			result.append(Integer.toHexString((0x000000ff & s[i]) | 0xffffff00).substring(6));
		}
		return result.toString();
	}
	
	

	/**
	 * 获取SHA-1 16位字节信息
	 * @param strPlain
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] sha(String strPlain){
		byte s[] = null;
		try {
			MessageDigest m= MessageDigest.getInstance("SHA-1");
			s = m.digest(strPlain.getBytes("utf-8"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return s;
	}
	
	
	/**
	 * Hash
	 * @param strPlain
	 * @return
	 * @throws Exception
	 */
	public static String hash(String strPlain){
		return String.valueOf(strPlain.hashCode());
//		MessageDigest md;
//		try {
//			md = MessageDigest.getInstance("SHA");
//			BASE64Encoder  base = new BASE64Encoder ();
//			String pwdAfter = base.encode(md.digest(strPlain.getBytes()));
//			return pwdAfter;
//		} catch (NoSuchAlgorithmException e) {
//			;
//		}
//		return "";
	}

	/**
   * Make a string representation of the exception.
   * @param e The exception to stringify
   * @return A string with exception name and call stack.
   */
  public static String stringifyException(Throwable e) {
    StringWriter stm = new StringWriter();
    PrintWriter wrt = new PrintWriter(stm);
    e.printStackTrace(wrt);
    wrt.close();
    return stm.toString();
  }
  
  
  /**
   * Returns an arraylist of strings.
   * @param str the comma seperated string values
   * @return the arraylist of the comma seperated string values
   */
  public static String[] getStrings(String str){
    Collection<String> values = getStringCollection(str);
    if(values.size() == 0) {
      return null;
    }
    return values.toArray(new String[values.size()]);
  }

  /**
   * Returns a collection of strings.
   * @param str comma seperated string values
   * @return an <code>ArrayList</code> of string values
   */
  public static Collection<String> getStringCollection(String str){
    List<String> values = new ArrayList<String>();
    if (str == null)
      return values;
    StringTokenizer tokenizer = new StringTokenizer (str,",");
    values = new ArrayList<String>();
    while (tokenizer.hasMoreTokens()) {
      values.add(tokenizer.nextToken());
    }
    return values;
  }
  
  /**
   * Given an array of strings, return a comma-separated list of its elements.
   * @param strs Array of strings
   * @return Empty string if strs.length is 0, comma separated list of strings
   * otherwise
   */
  
  public static String arrayToString(String[] strs) {
    if (strs.length == 0) { return ""; }
    StringBuffer sbuf = new StringBuffer();
    sbuf.append(strs[0]);
    for (int idx = 1; idx < strs.length; idx++) {
      sbuf.append(",");
      sbuf.append(strs[idx]);
    }
    return sbuf.toString();
  }
  
  /**
   * transform a Obejct 2 a String ,return value is "" when object is null
   * @param obj
   * @return
   * @author qiumm
   */
  public static String valueOf(Object obj){
	  return (obj == null) ? "" : obj.toString();
  }
  
  /**
   * get long value from a hex string 
   * @param hex
   * @return
   */
  public static Number getLongFromHex(String hex){
		BigInteger bigInt = new BigInteger(hex, 16);
		return bigInt;
  }
  
  /**
   * get the md5 value, and convert the md5 value to number code.
   * 
   * this method is used for get a unique code from a string
   * 
   * @param plain
   * @return
   */
  public static UUID getMd5UUID(String plain){
	  return UUIDUtils.getMd5UUID(plain);
  }
  
	/**
	 * convert a hex string to a byte array.
	 * 
	 * @param hex
	 * @return
	 */
	public static byte[] hexToByte(String hex) {
		int len = hex.length();
		byte[] value = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			value[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character
					.digit(hex.charAt(i + 1), 16));
		}
		return value;
	}

	private static final char[] BASE64_DIGITS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

	/**
	 * convert hex string to base64 string
	 * 
	 * @param hex
	 * @return
	 */
	public static String hexToBase64(String hex) {
		StringBuilder base64 = new StringBuilder();
		int group;
		for (int i = 0; i < 30; i += 6) {
			group = Integer.parseInt(hex.substring(i, i + 6), 16);
			base64.append(BASE64_DIGITS[(group >> 18) & 0x3f]);
			base64.append(BASE64_DIGITS[(group >> 12) & 0x3f]);
			base64.append(BASE64_DIGITS[(group >> 6) & 0x3f]);
			base64.append(BASE64_DIGITS[group & 0x3f]);
		}
		group = Integer.parseInt(hex.substring(30), 16);
		base64.append(BASE64_DIGITS[(group >> 2) & 0x3f]);
		base64.append(BASE64_DIGITS[(group << 4) & 0x3f]);
		base64.append("==");
		return base64.toString();
	}

	public static String join(Collection<?> list, String separator) {
		if(list==null){
			return null;
		}
		Iterator<?> iterator = list.iterator();
		// handle null, zero and one elements before building a buffer
		Object first = iterator.next();
		if (!iterator.hasNext()) {
			return valueOf(first);
		}
		// two or more elements
		StringBuffer buf = new StringBuffer(256); // Java default is 16,
													// probably too small
		if (first != null) {
			buf.append(first);
		}
		while (iterator.hasNext()) {
			if (separator != null) {
				buf.append(separator);
			}
			Object obj = iterator.next();
			if (obj != null) {
				buf.append(obj);
			}
		}
		return buf.toString();
	}
	
    /**
     * intersect 
     * 
     * @param str1
     * @param str2
     * @return
     */
    public static String intersect(String str1, String str2) {
        String targetString = "";
        // 取出其中较短的字符串(照顾效率)
        String shorter = str1.length() > str2.length() ? str2 : str1;
        String longer = shorter.equals(str1) ? str2 : str1;
        
        out:
        // 在较短的字符串中抽取其‘所有长度’的子串，顺序由长到短
        for(int subLength = shorter.length(); subLength > 0; subLength--){
            // 子串的起始角标由 0 开始右移，直至子串尾部与母串的尾部-重合为止
            for(int i = 0; i+subLength <= shorter.length(); i++){
                String subString = shorter.substring(i, i+subLength); // 取子串
                if(longer.indexOf(subString) >= 0){ // 注意 ‘=’
                    targetString = subString;
                    break out;  // 一旦满足条件，则最大子串即找到，停止循环，
                }
            }
        }
        return targetString;
    }

	public static String capitalize(String str) {
		int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        return new StringBuilder(strLen)
            .append(Character.toTitleCase(str.charAt(0)))
            .append(str.substring(1))
            .toString();
	}
	
	private static String SYMBOL_ALL_PATTERN = "^[ …—`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]*";
	private static Pattern allPattern = Pattern.compile(SYMBOL_ALL_PATTERN);
	
	public static boolean isSymbolOnly(String content){
		return allPattern.matcher(content).matches();
	}
}
