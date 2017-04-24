package com.xiongbeer.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * Created by shaoxiong on 17-4-23.
 */
public class Server {
    private final String host;
    private final int port;

    public Server(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
    }

    public void bind(){
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootStrap = new ServerBootstrap();
            bootStrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChildChannelHandler())
                    .option(ChannelOption.SO_BACKLOG, 1024);

            ChannelFuture future = bootStrap.bind(port).sync();
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }



    private class ChildChannelHandler extends ChannelInitializer<SocketChannel>{

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            /* 处理半包 */
            socketChannel.pipeline().addLast(
                    new ProtobufVarint32FrameDecoder());

            /* Protobuf 解码器 */
            socketChannel.pipeline().addLast(new ProtobufDecoder(
                    ProcessDataProto
                            .ProcessData
                            .getDefaultInstance()));

            socketChannel.pipeline().addLast(
                    new ProtobufVarint32LengthFieldPrepender());

            socketChannel.pipeline().addLast(new ProtobufEncoder());
            socketChannel.pipeline().addLast(new ServerHandler());

        }
    }

    public static void main(String[] args){
        try {
            Server server = new Server("127.0.0.1:2181", 8080);
            server.bind();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
