package com.xiongbeer.webveins.example.webmagic;

import com.xiongbeer.webveins.service.local.Action;
import com.xiongbeer.webveins.service.local.Bootstrap;
import com.xiongbeer.webveins.service.local.ProcessDataProto;
import com.xiongbeer.webveins.utils.InitLogger;
import com.xiongbeer.webveins.utils.UrlFileLoader;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shaoxiong on 17-5-9.
 */
public class Crawler extends Action implements PageProcessor {
    private Site site = Site.me().setRetryTimes(3)
            .setSleepTime(1000).setUseGzip(true)
            .setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
    private static Set<String> newUrls = new HashSet<String>();
    private static Spider spider = Spider.create(new Crawler());
    private static ProcessDataProto.ProcessData.Builder builder =
            ProcessDataProto.ProcessData.newBuilder();

    @Override
    public boolean run(String urlFilePath) {
    	try {
			List<String> urlsList = new UrlFileLoader().readFileByLine(urlFilePath);
			for(String url:urlsList){
				spider.addUrl(url);
			}
			spider.run();
            Bootstrap.upLoadNewUrls(newUrls);
            return true;
    	} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return false;
    }

    @Override
    public Site getSite() {
        return site;
    }

    @Override
    public void process(Page page) {
        String html = page.getHtml().toString();
        String selfUrl = page.getUrl().toString();
        Pattern pattern = Pattern.compile("(?<=<a href=\")(?!" + selfUrl
                + ")https://en.wikipedia.org/wiki/.*?(?=\")");
        Matcher matcher = pattern.matcher(html);
        while(matcher.find()){
        	newUrls.add(matcher.group());
        }
    }

    public static void main(String[] args){
        InitLogger.init();
        Crawler crawler = new Crawler();
        Bootstrap bootstrap = new Bootstrap(crawler);
        bootstrap.runClient();
        try {
        	/* 短暂的等待，等待Client与Server建立长连接 */
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            /* 连接建立，告诉Server爬虫已经准备好啦！ */
			bootstrap.ready();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
