package com.xiongbeer.webveins.example;

import com.xiongbeer.webveins.service.Action;
import com.xiongbeer.webveins.service.Bootstrap;
import com.xiongbeer.webveins.utils.InitLogger;

/**
 * Created by shaoxiong on 17-5-4.
 */
public class Webmagic extends Action{


    @Override
    public void run(String urlFilePath) {
        System.out.println(urlFilePath + " GET");
    }

    public static void main(String[] args){
        InitLogger.init();
        Webmagic webmagic = new Webmagic();
        Bootstrap bootstrap = new Bootstrap(webmagic);
        bootstrap.runClient();
    }
}

