package com.xiongbeer.webveins.scheduler;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.xiongbeer.webveins.utils.MD5;

public class BloomVisitedTableTest {
	
	@Test
	public void testBloomFilter() throws Exception{
		String url = "https://www.baidu.com";
		String md = new MD5(url).getMD5();
		BloomVisitedTable bl = new BloomVisitedTable();
		bl.add(md);
		assertEquals(bl.isExist(md), true);
	}
}
