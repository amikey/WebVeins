package com.xiongbeer.webveins.spider;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.xiongbeer.webveins.scheduler.QueueFrontier;
import com.xiongbeer.webveins.utils.Site;

public class Spider {
	public static void main(String[] args) throws ClientProtocolException, IOException{
		Site site = new Site();
		site.setCharset("UTF-8")
			.setDomain("https://www.baidu.com")
			.setUserAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:52.0) Gecko/20100101 Firefox/52.0")
			.addHeader("Accept", "html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
			.addHeader("Accept-Language", "en-US,en;q=0.5")
			.addHeader("Accept-Encoding", "keep-alive");
		
		String[] urls = {
				"https://baidu.com",
				"https://hc.apache.org/httpcomponents-client-4.5.x/quickstart.html",
				"https://hc.apache.org/httpcomponents-client-4.5.x/tutorial/html/fundamentals.html",
				"https://github.com",
				"https://baidu.com"
		};
		
		
		QueueFrontier qf = new QueueFrontier();
		for(String url:urls){
			if(qf.setAdd(url)){
				qf.put(url);
			}
		}
		
		
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		while(qf.get()!=null){
			HttpGet httpget = new HttpGet(qf.get());
			CloseableHttpResponse response = httpclient.execute(httpget);
			try{
				System.out.println(response.getStatusLine() + ":" + qf.get());
				qf.next();
			} finally{
				response.close();
			}
		}
	}
}
