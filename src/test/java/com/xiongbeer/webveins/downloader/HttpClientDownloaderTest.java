package com.xiongbeer.webveins.downloader;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;

import com.xiongbeer.webveins.utils.Page;

import static org.junit.Assert.assertEquals;

public class HttpClientDownloaderTest {
	@Test
	public void downloadTest(){
		HttpClientDownloader downloader = new HttpClientDownloader();
		Page page = downloader.download("https://baidu.com");
		String htmlSource = page.getHtml().toString();
		int statusCode = page.getStatusCode();
		assertEquals(statusCode, HttpStatus.SC_OK);
	}
}
