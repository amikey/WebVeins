package com.xiongbeer.service;

import com.xiongbeer.Configuration;
import com.xiongbeer.InitLogger;
import com.xiongbeer.zk.task.TaskWorker;
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
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by shaoxiong on 17-4-23.
 */
public class Server {
    private static LinkedList<Channel> channels = new LinkedList<Channel>();
    private Logger logger = LoggerFactory.getLogger(Server.class);
    private final String host;
    private final int port;
    private TaskWorker taskWorker;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public static LinkedList<Channel> getChannels() {
        return channels;
    }

    public static void setChannels(LinkedList<Channel> channels) {
        Server.channels = channels;
    }

    public void sentData(ProcessDataProto.ProcessData data){
        for(Channel channel:channels){
            channel.pipeline().writeAndFlush(data);
        }
    }

    public Server(String host, int port, TaskWorker taskWorker) throws IOException {
        this.host = host;
        this.port = port;
        this.taskWorker = taskWorker;
    }

    public void bind(){
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootStrap = new ServerBootstrap();
            bootStrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChildChannelHandler())
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

            socketChannel.pipeline().addLast(new ServerHandler(taskWorker));

        }
    }

    public static void main(String[] args){
        try {
            InitLogger.init();
            Configuration.getInstance();
            final ProcessDataProto.ProcessData.Builder builder =
                    ProcessDataProto.ProcessData.newBuilder();
            builder.setStatus(ProcessDataProto.ProcessData.Status.NULL);
            builder.setUrlFilePath("xasd");

            final Server server = new Server(Configuration.LOCAL_HOST, Configuration.LOCAL_PORT, null);

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    server.sentData(builder.build());
                    System.out.println("sent message");
                }
            };
            Timer timer = new Timer();
            timer.schedule(task, 10000, 2000);

            server.bind();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
