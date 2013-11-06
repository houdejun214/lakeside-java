package com.lakeside.core.utils;

import org.junit.Test;

public class PatternUtilsTest {

	@Test
	public void test() {
	}
	
	@Test
	public void testTrimMatch() {
		String input="Voce e muito linda, podemos nos conhecer quando voce vier pra ca ? (@mihblueberry live on http://t.co/OhEQWcrf)";
		System.out.println(input);
		System.out.println(PatternUtils.trimMatch("http://[^ ]*", input));
		
		
		input="Voce e muito linda, podemos nos conhecer quando voce vier pra ca ? (@mihblueberry live on http://t.co/OhEQWcrf 的撒发生大幅  http://www.google.com .com)";
		System.out.println(input);
		System.out.println(PatternUtils.trimMatch("http://[^ ]*", input));
	}

}
