package com.lakeside.core.utils;

import org.junit.Test;

import java.util.UUID;

public class UUIDUtilsTest {

	@Test
	public void testEncodeMongoBinData() {
		UUID uuid = UUIDUtils.getMd5UUID("twitter159608684866052096");
		String encodeMongoBinData = UUIDUtils.encodeMongoBinData(uuid);
		System.out.println(encodeMongoBinData);
//		Assert.isTrue("bD9o0MXnyb83yAEjOEPkuQ==".equals(encodeMongoBinData));
//		
//		uuid = UUIDUtils.getMd5UUID("instagram126645544009480255_1336165");
//		System.out.println(uuid);
//		encodeMongoBinData = UUIDUtils.encodeMongoBinData(uuid);
//		System.out.println(encodeMongoBinData);
	}

}
