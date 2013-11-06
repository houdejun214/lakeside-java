package com.lakeside.core;

import static org.junit.Assert.*;

import org.junit.Test;

public class ArgOptionsTest {

	@Test
	public void test() {
		String[] args = new String[]{"-ip=123","--id=213","-ic","sdfd","-have"};
		ArgOptions options = new ArgOptions(args);
		assertEquals(options.get("ip",""), "123");
		assertEquals(options.get("id",""), "213");
		assertEquals(options.get("ic",""), "sdfd");
		assertTrue(options.haveArg("have"));
	}

}
