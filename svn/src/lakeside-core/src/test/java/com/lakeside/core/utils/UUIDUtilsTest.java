package com.lakeside.core.utils;

import java.util.UUID;

import org.junit.Test;

public class UUIDUtilsTest {

	@Test
	public void testEncodeMongoBinData() {
		UUID uuid = UUIDUtils.getMd5UUID("rupingan2011");
		String encodeMongoBinData = UUIDUtils.encodeMongoBinData(uuid);
		Assert.isTrue("bD9o0MXnyb83yAEjOEPkuQ==".equals(encodeMongoBinData));
		
		uuid = UUIDUtils.getMd5UUID("instagram126645544009480255_1336165");
		System.out.println(uuid);
		encodeMongoBinData = UUIDUtils.encodeMongoBinData(uuid);
		System.out.println(encodeMongoBinData);
		
	}

}
