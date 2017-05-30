package com.xiongbeer.webveins.service.protocol.handler;

import com.xiongbeer.webveins.Configuration;
import com.xiongbeer.webveins.saver.HDFSManager;
import com.xiongbeer.webveins.service.ProcessDataProto.ProcessData;
import com.xiongbeer.webveins.service.local.Action;
import com.xiongbeer.webveins.service.protocol.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by shaoxiong on 17-5-30.
 */
public class LocalCrawlerHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = LoggerFactory.getLogger(LocalCrawlerHandler.class);
    private static HDFSManager hdfsManager = new HDFSManager(Configuration.HDFS_SYSTEM_CONF
            , Configuration.HDFS_SYSTEM_PATH);
    private static ExecutorService crawlerLoop = Executors.newSingleThreadExecutor();
    private Action action;
    public LocalCrawlerHandler(Action action){
        this.action = action;
    }

    /**
     * 用户应该Override Action中的run方法
     * run方法实际上是传递了已经拿到的Url
     * 爬虫可以开始任务了
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        ctx.fireChannelRead(msg);
        ProcessData data = ((Message) msg).getBody();
    }

    private void process(ChannelHandlerContext ctx, ProcessData data) throws IOException {
        String urlFilePath = data.getUrlFilePath();
        logger.info("Crawler get the task:" + urlFilePath
                + "success at {}", new Date().toString());
        hdfsManager.downLoad(urlFilePath, Configuration.TEMP_DIR);

        /*
            true 标识run成功，返回READY状态，领取下一个任务
            false 标识run失败，返回NULL状态，放弃该任务，让其他爬虫去领取该任务
         */
        String localSavePath = Configuration.TEMP_DIR + '/'
                + data.getUrlFileName();
        boolean flag = action.run(localSavePath);
        /* 任务结束后删除url文件 */
        new File(localSavePath).delete();
        ProcessData.Builder builder = ProcessData.newBuilder();
        builder.setUrlFilePath("");
        builder.setUrlFileName(data.getUrlFileName());
        builder.setStatus(flag?ProcessData.Status.FINNISHED : ProcessData.Status.NULL);
        String result = flag?"successed":"failed";
        logger.info("Run task " + result);
        ctx.writeAndFlush(builder.build());
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
