package com.xiongbeer.webveins.example;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import com.xiongbeer.webveins.service.Action;
import com.xiongbeer.webveins.service.Bootstrap;
import com.xiongbeer.webveins.service.ProcessDataProto;
import com.xiongbeer.webveins.utils.InitLogger;

/**
 * Created by shaoxiong on 17-5-4.
 */
public class Webmagic extends Action implements PageProcessor{
	private Site site = Site.me().setRetryTimes(3)
			.setSleepTime(1000).setUseGzip(true)
			.setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
    
	@Override
    public void run(String urlFilePath) {
        System.out.println(urlFilePath + " GET");
    }
    
	@Override
	public Site getSite() {
		return site;
	}

	@Override
	public void process(Page page) {
		String html = page.getHtml().toString();
		Pattern pattern = Pattern.compile("(?<=<a href=\")(?!https://en.wikipedia.org/wiki/Wiki)https://en.wikipedia.org/wiki/.*?(?=\")");
		Matcher matcher = pattern.matcher(html);
		String mainName = "";
		while(matcher.find()){
			mainName = matcher.group();
			System.out.println(mainName);
		}
	}
	
    public static void main(String[] args){
    	InitLogger.init();
    	String startUrl = "https://en.wikipedia.org/wiki/Wiki";
        Spider.create(new Webmagic())
		        .addUrl(startUrl)
		        .addPipeline(new FilePipeline("/home/shaoxiong/Documents/crawdata"))
		        .run();
        /*
        InitLogger.init();	
        Webmagic webmagic = new Webmagic();
        ProcessDataProto.ProcessData.Builder builder =
                ProcessDataProto.ProcessData.newBuilder();
        builder.setStatus(ProcessDataProto.ProcessData.Status.READY);
        builder.setUrlFilePath("");
        Bootstrap bootstrap = new Bootstrap(webmagic);
        bootstrap.runClient();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bootstrap.ready(builder.build());
        */
    }
}

