package com.xiongbeer.webveins.service.local;

import com.xiongbeer.webveins.Configuration;
import com.xiongbeer.webveins.saver.HDFSManager;
import com.xiongbeer.webveins.service.ProcessDataProto;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by shaoxiong on 17-4-23.
 */
@ChannelHandler.Sharable
public class ClientHandler extends ChannelInboundHandlerAdapter {
    private Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private Action action;
    private static HDFSManager hdfsManager = new HDFSManager(Configuration.HDFS_SYSTEM_CONF
            , Configuration.HDFS_SYSTEM_PATH);

    public ClientHandler(Action action){
        this.action = action;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        Client.setChannel(channel);
        logger.info(channel.remoteAddress().toString() + " log in "
                + "at {}", new Date().toString());
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
        ProcessDataProto.ProcessData data =
                (ProcessDataProto.ProcessData) msg;
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
        ProcessDataProto.ProcessData.Builder builder =
                ProcessDataProto.ProcessData.newBuilder();
        builder.setUrlFilePath("");
        builder.setUrlFileName(data.getUrlFileName());
        builder.setStatus(flag?ProcessDataProto.ProcessData.Status.FINNISHED
                :ProcessDataProto.ProcessData.Status.NULL);
        String result = flag?"successed":"failed";
        logger.info("Run task " + result);
        ctx.writeAndFlush(builder.build());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.warn("Server disconnect.");
        Client.setChannel(null);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
