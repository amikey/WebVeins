package com.xiongbeer.webveins.service;

import com.xiongbeer.webveins.zk.manager.Manager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;

/**
 * Created by shaoxiong on 17-5-2.
 */
public class BalanceServer {
    private final String host;
    private final int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Manager manager;

    public BalanceServer(String host, int port, Manager manager) throws IOException {
        this.host = host;
        this.port = port;
        this.manager = manager;
    }

    public void bind(){
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootStrap = new ServerBootstrap();
            bootStrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new BalanceServerHandler(manager))
                    .option(ChannelOption.SO_BACKLOG, 1024);

            final ChannelFuture future = bootStrap.bind(port).sync();
            future.channel().closeFuture();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    public void stop(){
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}
