package com.xiongbeer.webveins.service.balance;

import com.xiongbeer.webveins.zk.manager.Manager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shaoxiong on 17-5-2.
 */
@ChannelHandler.Sharable
public class BalanceServerHandler extends ChannelInboundHandlerAdapter {
    private Manager manager;
    private Logger logger = LoggerFactory.getLogger(BalanceServerHandler.class);
    public BalanceServerHandler(Manager manager){
        this.manager = manager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
