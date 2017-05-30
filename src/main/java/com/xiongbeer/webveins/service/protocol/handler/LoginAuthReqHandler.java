package com.xiongbeer.webveins.service.protocol.handler;


import com.xiongbeer.webveins.service.protocol.message.Header;
import com.xiongbeer.webveins.service.protocol.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import com.xiongbeer.webveins.service.protocol.message.Message.Coderc;
import com.xiongbeer.webveins.service.ProcessDataProto.ProcessData;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by shaoxiong on 17-5-28.
 */
public class LoginAuthReqHandler extends ChannelInboundHandlerAdapter {
    private AtomicBoolean isLongConnection;
    private Message initMessage;

    public LoginAuthReqHandler(AtomicBoolean isLongConnection, Message initMessage){
        this.isLongConnection = isLongConnection;
        this.initMessage = initMessage;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("trying to login...");
        ctx.writeAndFlush(initMessage);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        Message message = (Message)msg;
        Coderc rc = Coderc.get(message.getHeader().getType());
        switch (rc) {
            case CRAWLER_RESP:
                isLongConnection.set(true);
                break;
            case SHELL_RESP:
                isLongConnection.set(false);
                break;
            default:
                System.out.println("login refused");
                ctx.close();
                return;
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.fireExceptionCaught(cause);
    }
}
