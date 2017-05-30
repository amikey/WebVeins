package com.xiongbeer.webveins.service.protocol.handler;

import com.xiongbeer.webveins.service.protocol.message.Header;
import com.xiongbeer.webveins.service.protocol.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.xiongbeer.webveins.service.protocol.message.Message.Coderc;

/**
 * Created by shaoxiong on 17-5-28.
 */
public class HeartBeatReqHandler extends ChannelInboundHandlerAdapter {
    private volatile ScheduledFuture<?> heartBeat;
    private AtomicBoolean isLongConnection;

    public HeartBeatReqHandler(AtomicBoolean isLongConnection){
        this.isLongConnection = isLongConnection;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        Message message = (Message)msg;
        if (isLongConnection.get() && message.getHeader() != null && message.getHeader().getType() == Coderc.LOGIN_RESP.getValue()) {
            heartBeat = ctx.executor().scheduleAtFixedRate(new HeartBeatReqHandler.HeartBeatTask(ctx), 0, 10, TimeUnit.SECONDS);
        } else if (message.getHeader() != null && message.getHeader().getType() == Coderc.HEART_BEAT_RESP.getValue()) {
            System.out.println("client receive server heart message : " + message);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private class HeartBeatTask implements Runnable {

        private final ChannelHandlerContext ctx;

        public HeartBeatTask(final ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            Message message = buildHeatBeat();
            System.out.println("client send heart message :　" + message);
            ctx.writeAndFlush(message);
        }

        private Message buildHeatBeat() {
            Message message = new Message();
            Header header = new Header();
            header.setType(Coderc.HEART_BEAT_REQ.getValue());
            message.setHeader(header);
            return message;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        if (heartBeat != null) {
            heartBeat.cancel(true);
            heartBeat = null;
        }
        ctx.fireExceptionCaught(cause);
    }
}
