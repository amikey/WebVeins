package com.xiongbeer.webveins.service.protocol.handler;



import com.xiongbeer.webveins.service.protocol.message.MessageType;
import com.xiongbeer.webveins.service.protocol.message.ProcessDataProto.ProcessData;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by shaoxiong on 17-5-28.
 */
public class LoginAuthReqHandler extends ChannelInboundHandlerAdapter {
    private ProcessData initMessage;
    private Channel channel;

    public LoginAuthReqHandler(ProcessData initMessage){
        this.initMessage = initMessage;
        this.channel = channel;
    }

    public LoginAuthReqHandler(Channel channel){
        this.channel = channel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("trying to login...");
        channel = ctx.channel();
        MessageType rc = MessageType.get((byte) initMessage.getType());
        switch (rc){
            case CRAWLER_REQ:
                ctx.fireChannelRead(buildFirstHeartBeat());
                break;
            case SHELL_REQ:
                break;
            default:
                break;
        }
        ctx.writeAndFlush(initMessage);
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
                System.out.println("get");
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
        channel = null;
    }

    private ProcessData buildFirstHeartBeat(){
        ProcessData.Builder builder = ProcessData.newBuilder();
        builder.setType(MessageType.HEART_BEAT_REQ.getValue());
        return builder.build();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.fireExceptionCaught(cause);
    }
}
