package com.xiongbeer.webveins.service.protocol.handler;

import com.xiongbeer.webveins.service.protocol.Client;
import com.xiongbeer.webveins.service.protocol.message.MessageType;
import com.xiongbeer.webveins.service.protocol.message.ProcessDataProto.ProcessData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by shaoxiong on 17-5-28.
 */
public class HeartBeatReqHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = LoggerFactory.getLogger(LocalCrawlerHandler.class);
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private volatile ScheduledFuture<?> heartBeat;
    private AtomicBoolean closeLongConnection;
    private Client client;

    public HeartBeatReqHandler(Client client, AtomicBoolean closeLongConnection){
        this.client = client;
        this.closeLongConnection = closeLongConnection;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        ProcessData message = (ProcessData)msg;
        if (message.getType() == MessageType.HEART_BEAT_REQ.getValue()) {
            heartBeat = ctx
                    .channel()
                    .eventLoop()
                    .scheduleAtFixedRate(new HeartBeatReqHandler.HeartBeatTask(ctx), 10, 10, TimeUnit.SECONDS);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        executor.execute(new KeepConnection(closeLongConnection));
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

    private class KeepConnection implements Runnable {
        private AtomicBoolean closeLongConnection;

        public KeepConnection(AtomicBoolean closeLongConnection){
            this.closeLongConnection = closeLongConnection;
        }

        @Override
        public void run() {
            reconnect();
        }

        public void reconnect() {
            logger.error("lose connection, trying to reconncet...");
            if(!closeLongConnection.get()) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                    client.connect();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e){
                    reconnect();
                }
            }
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
            ctx.writeAndFlush(message);
        }

        private ProcessData buildHeatBeat() {
            ProcessData.Builder builder = ProcessData.newBuilder();
            builder.setType(MessageType.HEART_BEAT_REQ.getValue());
            return builder.build();
        }
    }
}
