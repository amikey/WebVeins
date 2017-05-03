package com.xiongbeer.webveins.service;

import com.xiongbeer.webveins.zk.manager.Manager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by shaoxiong on 17-5-2.
 */
public class BalanceServer {
    private static LinkedList<Channel> channels = new LinkedList<Channel>();
    private Logger logger = LoggerFactory.getLogger(BalanceServer.class);
    private final String host;
    private final int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Manager manager;

    public static LinkedList<Channel> getChannels() {
        return channels;
    }

    public static void setChannels(LinkedList<Channel> channels) {
        BalanceServer.channels = channels;
    }

    public void sentData(ProcessDataProto.ProcessData data){
        for(Channel channel:channels){
            channel.pipeline().writeAndFlush(data);
        }
    }

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
                    .childHandler(new BalanceServer.ChildChannelHandler())
                    .option(ChannelOption.SO_BACKLOG, 1024);

            final ChannelFuture future = bootStrap.bind(port).sync();
            future.channel().closeFuture().sync();

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

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            /* 处理半包 */
            socketChannel.pipeline().addLast(
                    new ProtobufVarint32FrameDecoder());

            /* Protobuf 解码器 */
            socketChannel.pipeline().addLast(new ProtobufDecoder(
                    BalanceDataProto
                            .BalanceData
                            .getDefaultInstance()));

            socketChannel.pipeline().addLast(
                    new ProtobufVarint32LengthFieldPrepender());

            socketChannel.pipeline().addLast(new ProtobufEncoder());

            socketChannel.pipeline().addLast(new BalanceServerHandler(manager));
        }
    }
}
