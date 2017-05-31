package com.xiongbeer.webveins.service.protocol.handler;

import com.xiongbeer.webveins.service.protocol.message.MessageType;
import com.xiongbeer.webveins.service.protocol.message.ProcessDataProto.ProcessData;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by shaoxiong on 17-5-28.
 */
public class LoginAuthRespHandler extends ChannelInboundHandlerAdapter {
    private Map<String, Boolean> nodeCheck = new ConcurrentHashMap<String, Boolean>();
    private String[] whitekList = {"127.0.0.1", "192.168.1.104"};
    private Set<Channel> channels;

    public LoginAuthRespHandler(Set<Channel> channels){
        this.channels = channels;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        ProcessData message = (ProcessData)msg;
        MessageType rc = MessageType.get((byte) message.getType());
        switch (rc){
            case CRAWLER_REQ:
                break;
            case SHELL_REQ:
                break;
            case HEART_BEAT_REQ:
                break;
            default:
                System.out.println("Closed.");
                ctx.close();
                return;
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channels.remove(ctx.channel());
    }

    private void login(ChannelHandlerContext ctx, Object msg){
        //TODO
        channels.add(ctx.channel());
    }

    private ProcessData buildResp() {
        ProcessData.Builder builder = ProcessData.newBuilder();
        builder.setType(MessageType.HEART_BEAT_RESP.getValue());
        return builder.build();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        nodeCheck.remove(ctx.channel().remoteAddress().toString()); //出现异常删除缓存
        ctx.close();
        ctx.fireExceptionCaught(cause);
    }
}
