package com.xiongbeer.webveins.example.webmagic;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xiongbeer.webveins.service.local.CrawlerBootstrap;
import com.xiongbeer.webveins.utils.InitLogger;

import io.netty.util.internal.ConcurrentSet;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * 整个系统最开始是没有任务列表的，需要一个预热来导入一部分url数据
 * 这个类就是用来做预热的
 *
 * Created by shaoxiong on 17-5-9.
 */
public class WarmUp implements PageProcessor {
    private Site site = Site.me().setRetryTimes(3)
            .setSleepTime(1000).setUseGzip(true)
            .setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
    private static Set<String> newUrls = new ConcurrentSet<String>();
    
    @Override
    public void process(Page page) {
        /* 只要获取的url数量大于100就终止爬虫任务 */
    	if(newUrls.size() > 100){
    		try {
				CrawlerBootstrap.upLoadNewUrls(newUrls);
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
