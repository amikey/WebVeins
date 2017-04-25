package com.xiongbeer.service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by shaoxiong on 17-4-23.
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {
    private final ByteBuf firstMessage;
    private  byte[] req =
            ("TIME" + System.getProperty("line.separator")).getBytes();
    public ClientHandler(){

        firstMessage = Unpooled.buffer(req.length);
        firstMessage.writeBytes(req);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ProcessDataProto.ProcessData.Builder builder =
                ProcessDataProto.ProcessData.newBuilder();
        builder.setUrlFilePath("sddd");
        ctx.writeAndFlush(Unpooled.copiedBuffer(("123" + System.getProperty("line.separator")).getBytes()));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
