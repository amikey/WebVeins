package com.xiongbeer.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by shaoxiong on 17-4-23.
 */
public class ServerHandler extends ChannelInboundHandlerAdapter{
    private final ServerData serverData;
    private static int STEP = 1;

    public ServerHandler(ServerData serverData){
        this.serverData = serverData;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.serverData.addLoad(STEP);
        System.out.println(serverData);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.serverData.reduceLoad(STEP);
        System.out.println(serverData);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
