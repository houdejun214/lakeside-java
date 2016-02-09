package com.lakeside.core.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringUtilsTest {

	@Test
	public void test() throws Exception {
		String name = "redispasswd123";
		String md5 = StringUtils.md5Encode(name);
		System.out.println(md5);
		
		String convert = convert("我们是中国人asdfsdfsd");
		System.out.println(convert);
	}
	
	public String convert(String str) {
		str = (str == null ? "" : str);
		String tmp;
		StringBuffer sb = new StringBuffer(1000);
		char c;
		int i, j;
		sb.setLength(0);
		for (i = 0; i < str.length(); i++) {
			c = str.charAt(i);
			if(!isChinese(c)){
				sb.append(c);
				continue;
			}
			sb.append("\\u");
			j = (c >>> 8); // 取出高8位
			tmp = Integer.toHexString(j);
			if (tmp.length() == 1)
				sb.append("0");
			sb.append(tmp);
			j = (c & 0xFF); // 取出低8位
			tmp = Integer.toHexString(j);
			if (tmp.length() == 1)
				sb.append("0");
			sb.append(tmp);

		}
		return (new String(sb));
	} 
	
	public static boolean isChinese(char a) {
	     int v = (int)a;
	     return (v >=19968 && v <= 171941);
	}
	
	@Test
	public void formatTest(){
		System.out.println(StringUtils.format("this.is {0}, {0},{1}", 0,"women"));
	}

    @Test
    public void testTokenizeToStringArray() {
        String[] stringArray = StringUtils.tokenizeToStringArray("/root/second/third/", "/");
        assertEquals(3, stringArray.length);
    }

    @Test
    public void testTokenizeToStringArrayIgnoreEmpty() {
        String[] stringArray = StringUtils.tokenizeToStringArray("/root/second//third/", "/");
        assertEquals(3, stringArray.length);

        stringArray = StringUtils.tokenizeToStringArray("http://www.amazon.com/s/ref=lp_7147441011_ex_n_1?rh=n%3A7141123011%2Cn%3A10445813011/", "/");
        assertEquals(4, stringArray.length);
    }

    @Test
    public void testSplit() {
        String[] stringArray = StringUtils.split("/root/second//third/", "/");
        assertEquals(3, stringArray.length);

        stringArray = StringUtils.split("http://www.amazon.com/s/ref=lp_7147441011_ex_n_1?rh=n%3A7141123011%2Cn%3A10445813011/", "/");
        assertEquals(4, stringArray.length);
    }
}
