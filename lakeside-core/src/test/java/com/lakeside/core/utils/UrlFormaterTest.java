package com.lakeside.core.utils;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UrlFormaterTest {

	@Test
	public void testUrlFormater() {
		
		String s = "http://www.google.com/?q={query}&q1={query}{loc}&m=abc&loc={loc}&n=12&loc1={loc}";
		System.out.println(s);
		UrlFormater formater = new UrlFormater(s);
		List<String> list = formater.getFormatNameList();
		for(String name:list){
			System.out.println(name);
		}
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("query", "queryValue");
		param.put("loc", "locValue");
		System.out.println(formater.format(param));
	}

	@Test
	public void testFormat() {
		String s = "http://www.google.com/?q={query}&q1={query}{loc}&m=abc&loc={loc}&n=12&loc1={loc}";
		System.out.println(s);
		UrlFormater formater = new UrlFormater(s);
		List<String> list = formater.getFormatNameList();
		for(String name:list){
			System.out.println(name);
		}
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("query", "queryValue");
		System.out.println(formater.format(param));
	}

	@Test
	public void testFormat1() {
		String s = "http://www.google.com/?q={query}&q1={query}{loc}&m=abc&loc={loc}&n=12&loc1={loc}";
		System.out.println(s);
		UrlFormater formater = new UrlFormater(s);
		List<String> list = formater.getFormatNameList();
		for(String name:list){
			System.out.println(name);
		}
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("query", "queryValue");
		System.out.println(formater.format(param));
	}
	
	@Test
	public void testFormat2() {
		String s = "http://www.google.com/?q={query}&q1={query}{loc}&m=abc&loc={loc}&n=12&loc1={loc}";
		System.out.println(s);
		UrlFormater formater = new UrlFormater(s);
		List<String> list = formater.getFormatNameList();
		for(String name:list){
			System.out.println(name);
		}
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("query", "queryValue");
		param.put("loc", "locValue");
		System.out.println(formater.format(param));
	}
	
	@Test(expected=RuntimeException.class)
	public void testFormat3() {
		String s = "http://www.google.com/{path}/?q={query}&q1={query}{loc}&m=abc&loc={loc}&n=12&loc1={loc}";
		System.out.println(s);
		UrlFormater formater = new UrlFormater(s);
		List<String> list = formater.getFormatNameList();
		for(String name:list){
			System.out.println(name);
		}
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("query", "queryValue");
		param.put("loc", "locValue");
		System.out.println(formater.format(param));
	}
	
	@Test
	public void testFormat4() {
		String s = "http://www.google.com/{path}/?q={query}&q1={query}{loc}&m=abc&loc={loc}&n=12&loc1={loc}";
		System.out.println(s);
		UrlFormater formater = new UrlFormater(s);
		List<String> list = formater.getFormatNameList();
		for(String name:list){
			System.out.println(name);
		}
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("path", "path1");
		param.put("query", "queryValue");
		param.put("loc", "locValue");
		System.out.println(formater.format(param));
	}
	
	@Test
	public void testFormat5() {
		String s = "http://www.google.com/{path}/{path2}/?q={query}&q1={query}{loc}&m=abc&loc={loc}&n=12&loc1={loc}";
		System.out.println(s);
		UrlFormater formater = new UrlFormater(s);
		List<String> list = formater.getFormatNameList();
		for(String name:list){
			System.out.println(name);
		}
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("path2", "path2");
		param.put("path", "path1");
		param.put("query", "queryValue");
		param.put("loc", "locValue");
		System.out.println(formater.format(param));
	}
	
	@Test
	public void testFormat6() {
		String s = "http://www.google.com/{path}/{path2}/?q={query}&q1={query}{loc}&m=abc&loc={loc}sdf&n=12&loc1={loc}";
		System.out.println(s);
		UrlFormater formater = new UrlFormater(s);
		List<String> list = formater.getFormatNameList();
		for(String name:list){
			System.out.println(name);
		}
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("path2", "path2");
		param.put("path", "path1");
		param.put("query", "queryValue");
		param.put("loc", "locValue");
		System.out.println(formater.format(param));
	}
}
