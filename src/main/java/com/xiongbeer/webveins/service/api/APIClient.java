package com.xiongbeer.webveins.service.api;

import com.xiongbeer.webveins.Configuration;
import com.xiongbeer.webveins.utils.Color;
import com.xiongbeer.webveins.utils.InitLogger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * Created by shaoxiong on 17-5-13.
 */
public class APIClient {
    private String command;
    public APIClient(String command){
        this.command = command;
    }

    public void run(int port){
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel)
                            throws Exception {
                        channel.pipeline().addLast(new LineBasedFrameDecoder(1024));
                        channel.pipeline().addLast(new StringDecoder());
                        channel.pipeline().addLast(new APIClientHandler(command));
                    }
                });
        try {
            ChannelFuture future = bootstrap
                    .connect("localhost", port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
          group.shutdownGracefully();
        }
    }

    public static void main(String[] args){
        InitLogger.initEmpty();

        Configuration.getInstance();
        int port = Configuration.LOCAL_SHELL_PORT;
        StringBuilder command = new StringBuilder();
        for(String arg:args){
            command.append(arg);
            command.append(" ");
        }
        System.out.println("[info] command: " + command.toString());
        APIClient client = new APIClient(command.toString());
        try {
            client.run(port);
        } catch (Throwable e) {
            System.out.println(Color.error("[Error] Connect to api server failed:"
                    + " Please make sure local server service has started."));
            System.exit(1);
        }
    }
}
