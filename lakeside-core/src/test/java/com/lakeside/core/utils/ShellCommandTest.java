package com.lakeside.core.utils;

import org.junit.Test;

public class ShellCommandTest {

	@Test
	public void testGetLinuxOSDistributionName() {
		String result = LinuxSystemUtils.getLinuxOSDistributionName();
		if (LinuxSystemUtils.IS_OS_LINUX) {
			System.out.println(result);
			Assert.notNull(result);
		}
	}

}
