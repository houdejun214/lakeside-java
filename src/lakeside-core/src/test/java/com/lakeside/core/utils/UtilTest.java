package com.lakeside.core.utils;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class UtilTest {

	@Test
	public void testRandom() {
		System.out.print(Arrays.toString(Util.random(10, 5)));
		System.out.print(Arrays.toString(Util.random(10, 5)));
		System.out.print(Arrays.toString(Util.random(10, 5)));
	}

}
