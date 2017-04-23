package com.xiongbeer.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * Created by shaoxiong on 17-4-23.
 */
public class Server {
    private final ServerData serverData;
    private final ZooKeeper zk;

    private static final int SESSION_TIME_OUT = 2000;

    public Server(String zkHostPort, ServerData serverData) throws IOException {
        this.zk = new ZooKeeper(zkHostPort, SESSION_TIME_OUT, null);
        this.serverData = serverData;
    }

    public void bind(){
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootStrap = new ServerBootstrap();
            bootStrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel socketChannel){
                            socketChannel.pipeline()
                                    .addLast(new ServerHandler(serverData));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootStrap.bind(serverData.getPort()).sync();
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args){
        ServerData serverData = new ServerData("127.0.0.1", 8080);
        try {
            Server server = new Server("127.0.0.1:2181", serverData);
            server.bind();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
