package com.xiongbeer.webveins.service.protocol.handler;

import com.xiongbeer.webveins.service.protocol.message.MessageType;
import com.xiongbeer.webveins.service.protocol.message.ProcessDataProto.ProcessData;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Set;


/**
 * Created by shaoxiong on 17-5-28.
 */
public class LoginAuthRespHandler extends ChannelInboundHandlerAdapter {
    private final static Logger logger = LoggerFactory.getLogger(LoginAuthRespHandler.class);
    private String[] whitekList = {"127.0.0.1", "192.168.1.104"};
    private Set<Channel> channels;

    public LoginAuthRespHandler(Set<Channel> channels){
        this.channels = channels;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if(login(ctx)){
            channels.add(ctx.channel());
        } else {
            logger.error("Reject connection request: " + ctx.channel());
            ctx.close();
        }
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
                logger.error("Unknow request rc-code: " + rc);
                ctx.close();
                return;
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channels.remove(ctx.channel());
    }



    private boolean login(ChannelHandlerContext ctx){
        if(!channels.contains(ctx.channel())) {
            for(String ip:whitekList){
                InetSocketAddress address = (InetSocketAddress)ctx.channel().remoteAddress();
                if(ip.equals(address.getAddress().getHostAddress())){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
    }
}
