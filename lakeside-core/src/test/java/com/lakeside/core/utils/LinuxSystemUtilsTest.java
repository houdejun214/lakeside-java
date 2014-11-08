package com.lakeside.core.utils;

import org.junit.Test;

public class LinuxSystemUtilsTest {

	@Test
	public void test() {
		String result = ShellCommand.execute("uname -{0}", "o");
		if (LinuxSystemUtils.IS_OS_LINUX) {
			System.out.println(result);
			Assert.notNull(result);
		}
	}
}
