package com.xiongbeer.webveins.service.protocol.handler;

import com.xiongbeer.webveins.service.protocol.message.Header;
import com.xiongbeer.webveins.service.protocol.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import com.xiongbeer.webveins.service.protocol.message.Message.Coderc;

/**
 * Created by shaoxiong on 17-5-28.
 */
public class HeartBeatRespHandler extends ChannelInboundHandlerAdapter {

    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        Message message = (Message)msg;
        if (message.getHeader() != null && message.getHeader().getType() == Coderc.HEART_BEAT_REQ.getValue()) {
            System.out.println("server receive client heart message " + message);
            Message heartBeat = buildHeartBeat();
            ctx.writeAndFlush(heartBeat);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    public Message buildHeartBeat() {
        Message message = new Message();
        Header header = new Header();
        header.setType(Coderc.HEART_BEAT_RESP.getValue());  //心跳应答消息
        message.setHeader(header);
        return message;
    }
}
