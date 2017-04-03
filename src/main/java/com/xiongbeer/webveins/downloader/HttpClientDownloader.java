package com.xiongbeer.webveins.downloader;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.xiongbeer.webveins.utils.Page;
import com.xiongbeer.webveins.utils.Site;

public class HttpClientDownloader {
	private Site site;
	private CloseableHttpClient getHttpClient(){
		CloseableHttpClient httpClient = HttpClients.createDefault();	
		return httpClient;
	}
	
	
	private HttpGet setHttpGet(HttpGet httpGet){
		if(site == null){
			return httpGet;
		}
		return httpGet;
	}
	public HttpClientDownloader(){}
	public HttpClientDownloader(Site site){
		this.site = site;
	}

	public Page download(String url){
		Page page = new Page();
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpClient httpClient = getHttpClient();
		try {
			CloseableHttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			page.setHtml(EntityUtils.toString(entity));
			page.setStatusCode(response.getStatusLine().getStatusCode());
			response.close();
		} catch (ClientProtocolException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}finally{
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return page;
	}
	
}
