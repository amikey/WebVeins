package com.xiongbeer.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by shaoxiong on 17-4-23.
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {
    private Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private Action action;

    public ClientHandler(Action action){
        this.action = action;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Server.getChannels().add(ctx.channel());
        logger.info(ctx.channel().remoteAddress().toString() + " log in "
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
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ProcessDataProto.ProcessData data =
                (ProcessDataProto.ProcessData) msg;
        String urlFilePath = data.getUrlFilePath();
        action.run(urlFilePath);
        logger.info("Crawler get the task success at {}", new Date().toString());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Server.getChannels().remove(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
