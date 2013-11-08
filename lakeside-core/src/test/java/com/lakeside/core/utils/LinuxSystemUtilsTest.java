package com.lakeside.core.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class LinuxSystemUtilsTest {

	@Test
	public void test() {
		String result = ShellCommand.execute("uname -{0}", "o");
		System.out.println(result);
		Assert.notNull(result);
	}

}
