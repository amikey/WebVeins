package com.xiongbeer.webveins.service.api;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


/**
 * Created by shaoxiong on 17-5-13.
 */
public class APIClientHandler extends ChannelInboundHandlerAdapter{
    private String command;

    public APIClientHandler(String command){
        this.command = command;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        System.out.println(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        byte[] content = (command + System.getProperty("line.separator"))
                .getBytes();
        ByteBuf message = Unpooled.buffer(content.length);
        message.writeBytes(content);
        ctx.writeAndFlush(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
    }
}
