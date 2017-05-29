package com.xiongbeer.webveins.service.protocol;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import com.xiongbeer.webveins.service.protocol.Message.Coderc;

/**
 * Created by shaoxiong on 17-5-28.
 */
public class LoginAuthReqHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("gonna login in");
        ctx.writeAndFlush(buildLoginReq());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        Message message = (Message)msg;
        if (message.getHeader() != null && message.getHeader().getType() == Coderc.LOGIN_RESP.getValue()) {
            byte loginResult = (byte)message.getBody();
            if (loginResult != Coderc.LOGIN_ACCEPTED.getValue()) {
                ctx.close();
            } else {
                System.out.println("login is ok " + message);
                ctx.fireChannelRead(msg);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    public Message buildLoginReq() {
        Message message = new Message();
        Header header = new Header();
        header.setType(Coderc.LOGIN_REQ.getValue());
        message.setHeader(header);
        return message;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.fireExceptionCaught(cause);
    }
}
