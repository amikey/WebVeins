package com.xiongbeer.webveins.downloader;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import com.xiongbeer.webveins.utils.Site;

public class HttpClientGenerator {
	public CloseableHttpClient generateClient(Site site){
		HttpClientBuilder httpClientBuilder = HttpClients.custom();
        if (site != null && site.getUserAgent() != null) {
            httpClientBuilder.setUserAgent(site.getUserAgent());
        } else {
            httpClientBuilder.setUserAgent("");
        }
       
		return httpClientBuilder.build();
	}
}
