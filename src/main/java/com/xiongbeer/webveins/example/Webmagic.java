package com.xiongbeer.webveins.example;

import com.xiongbeer.webveins.service.Action;
import com.xiongbeer.webveins.service.Bootstrap;
import com.xiongbeer.webveins.service.ProcessDataProto;
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
        //bootstrap.ready(builder.build());
    }
}

