package com.xiongbeer.webveins.service;

import com.xiongbeer.webveins.Configuration;
import com.xiongbeer.webveins.utils.InitLogger;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by shaoxiong on 17-5-2.
 */
public class BalanceClient {
    private static Channel channel;
    public void connect(String host, int port) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            io.netty.bootstrap.Bootstrap b = new io.netty.bootstrap.Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new BalanceClientHandler());
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public void disconnect(){
        channel.close();
    }

    public static void main(String[] args){
        new BalanceClient().connect("localhost", 8080);
    }
}
