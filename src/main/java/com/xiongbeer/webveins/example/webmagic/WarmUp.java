package com.xiongbeer.webveins.example.webmagic;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xiongbeer.webveins.service.local.Bootstrap;
import com.xiongbeer.webveins.utils.InitLogger;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * Created by shaoxiong on 17-5-9.
 */
public class WarmUp implements PageProcessor {
    private Site site = Site.me().setRetryTimes(3)
            .setSleepTime(1000).setUseGzip(true)
            .setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
    private static Set<String> newUrls = new HashSet<String>();
    
    @Override
    public void process(Page page) {
    	if(newUrls.size() > 100){
    		try {
				Bootstrap.upLoadNewUrls(newUrls);
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
    		return;
    	}
        String html = page.getHtml().toString();
        String selfUrl = page.getUrl().toString();
        Pattern pattern = Pattern.compile("(?<=<a href=\")(?!" + selfUrl
                + ")https://en.wikipedia.org/wiki/.*?(?=\")");
        Matcher matcher = pattern.matcher(html);
        int counter = 0;
        while(matcher.find()){
        	if(counter < 5){
        		page.addTargetRequest(matcher.group());
        		++counter;
        	}
        	else{
        		newUrls.add(matcher.group());
        	}
        }
        System.out.println("current size: " + newUrls.size());
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args){
        InitLogger.init();
        String startUrl = "https://en.wikipedia.org/wiki/Wiki";
        Spider.create(new WarmUp())
                .addUrl(startUrl)
                .thread(1)
                .run();
    }
}
