package com.lakeside.core.utils;

import org.junit.Test;

import java.util.Arrays;

public class UtilTest {

	@Test
	public void testRandom() {
		System.out.print(Arrays.toString(Util.random(10, 5)));
		System.out.print(Arrays.toString(Util.random(10, 5)));
		System.out.print(Arrays.toString(Util.random(10, 5)));
	}

}
