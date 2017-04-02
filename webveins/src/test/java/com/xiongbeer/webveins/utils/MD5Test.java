package com.xiongbeer.webveins.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MD5Test {
	
	@Test
	public void testGetMD5() throws Exception{
		String url = "https://www.baidu.com";
		String result = "f9751de431104b125f48dd79cc55822a";
		MD5 md = new MD5(url);
		assertEquals(result, md.getMD5());
	}
}
