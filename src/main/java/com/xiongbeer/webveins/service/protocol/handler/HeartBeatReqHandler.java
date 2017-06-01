package com.xiongbeer.webveins.service.protocol.handler;

import com.xiongbeer.webveins.service.protocol.message.MessageType;
import com.xiongbeer.webveins.service.protocol.message.ProcessDataProto.ProcessData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by shaoxiong on 17-5-28.
 */
public class HeartBeatReqHandler extends ChannelInboundHandlerAdapter {
    private volatile ScheduledFuture<?> heartBeat;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        ProcessData message = (ProcessData)msg;
        int rc = message.getType();
        if (rc == MessageType.HEART_BEAT_REQ.getValue()) {
            heartBeat = ctx
                    .channel()
                    .eventLoop()
                    .scheduleAtFixedRate(new HeartBeatReqHandler.HeartBeatTask(ctx), 10, 10, TimeUnit.SECONDS);
        } else if(rc == MessageType.HEART_BEAT_RESP.getValue()){
            System.out.println("client receive server heart message : " + message);
        }
    }

    private class HeartBeatTask implements Runnable {

        private final ChannelHandlerContext ctx;

        public HeartBeatTask(final ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            ProcessData message = buildHeatBeat();
            System.out.println("client send heart message :　" + message);
            ctx.writeAndFlush(message);
        }

        private ProcessData buildHeatBeat() {
            ProcessData.Builder builder = ProcessData.newBuilder();
            builder.setType(MessageType.HEART_BEAT_REQ.getValue());
            return builder.build();
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
