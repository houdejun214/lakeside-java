package com.lakeside.data.sqldb;

import static org.junit.Assert.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class MysqlDataSourceTest {

	@Test
	public void test() {
		assertFalse(isReplicationConnection("jdbc:mysql://1.1.1.1/test?useUnicode=true&characterEncoding=UTF-8&charSet=UTF-8"));
		assertTrue(isReplicationConnection("jdbc:mysql://1.1.1.1,1.1.1.2/test?useUnicode=true&characterEncoding=UTF-8&charSet=UTF-8"));
		assertTrue(isReplicationConnection("jdbc:mysql://1.1.1.1:3306,1.1.1.2:3306/test?useUnicode=true&characterEncoding=UTF-8&charSet=UTF-8"));
		assertTrue(isReplicationConnection("jdbc:mysql://1.1.1.1,1.1.1.2,1.1.1.3/test?useUnicode=true&characterEncoding=UTF-8&charSet=UTF-8"));
		assertTrue(isReplicationConnection("jdbc:mysql://next-1,next-2/test?useUnicode=true&characterEncoding=UTF-8&charSet=UTF-8"));
		assertFalse(isReplicationConnection("jdbc:mysql://next-2/test?useUnicode=true&characterEncoding=UTF-8&charSet=UTF-8"));
		assertFalse(isReplicationConnection("jdbc:mysql://next-2:123/test?useUnicode=true&characterEncoding=UTF-8&charSet=UTF-8"));
	}

	
	protected boolean isReplicationConnection(String jdbcurl){
		String regex = "jdbc:mysql://([^/]+)/.*";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(jdbcurl);
		while(matcher.find()){
			String hosts = matcher.group(1);
			String[] split = hosts.trim().split(",");
			if(split.length>1){
				return true;
			}
		}
		return false;
	}
}
