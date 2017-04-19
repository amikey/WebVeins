package com.xiongbeer.filter.bloom;

import com.xiongbeer.Configuration;
import com.xiongbeer.MD5;
import com.xiongbeer.VeinsException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by shaoxiong on 17-4-12.
 */
public class UrlFilterTest {
    UrlFilter filter = new UrlFilter(2 << 24, 0.0000001, UrlFilter.CreateMode.RAM);

    MD5 url1 = new MD5("https://www.baidu.com");
    MD5 url2 = new MD5("http://www.baidu.com");
    MD5 url3 = new MD5("https://www.zhihu.com");
    MD5 url4 = new MD5("1");
    MD5 url5 = new MD5("2");

    @Test
    public void addAndExistTest() throws VeinsException.FilterOverflowException {
        filter.add(url1.getMD5());
        assertEquals(filter.exist(url1.getMD5()), true);
        assertEquals(filter.exist(url2.getMD5()), false);
        filter.add(url2.getMD5());
        assertEquals(filter.exist(url2.getMD5()), true);
        assertEquals(filter.exist(url3.getMD5()), false);
        filter.add(url3.getMD5());
        assertEquals(filter.exist(url3.getMD5()), true);
        filter.add(url4.getMD5());
        assertEquals(filter.exist(url4.getMD5()), true);
        filter.add(url5.getMD5());
        assertEquals(filter.exist(url5.getMD5()), true);
    }
}
