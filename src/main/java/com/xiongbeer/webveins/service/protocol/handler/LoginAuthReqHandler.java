package com.xiongbeer.webveins.service.protocol.handler;

import com.xiongbeer.webveins.service.protocol.message.MessageType;
import com.xiongbeer.webveins.service.protocol.message.ProcessDataProto.ProcessData;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shaoxiong on 17-5-28.
 */
public class LoginAuthReqHandler extends ChannelInboundHandlerAdapter {
    private Logger logger = LoggerFactory.getLogger(LoginAuthReqHandler.class);
    /* initMessage不为空说明是短连接，反之则为长连接，长连接需要将channel加入缓存 */
    private ProcessData initMessage;
    private Channel[] channel;

    public LoginAuthReqHandler(ProcessData initMessage){
        this.initMessage = initMessage;
    }

    public LoginAuthReqHandler(Channel[] channel){
        this.channel = channel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if(initMessage != null){
            ctx.writeAndFlush(initMessage);
        } else {
            logger.info("build connection success.");
            channel[0] = ctx.channel();
            ProcessData.Builder builder = ProcessData.newBuilder();
            builder.setType(MessageType.HEART_BEAT_REQ.getValue());
            ctx.fireChannelRead(builder.build());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        ProcessData message = (ProcessData) msg;
        MessageType rc = MessageType.get((byte) message.getType());
        switch (rc) {
            case CRAWLER_RESP:
                break;
            case SHELL_RESP:
                break;
            case HEART_BEAT_RESP:
                break;
            default:
                System.out.println("login refused");
                ctx.close();
                return;
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
    }
}
