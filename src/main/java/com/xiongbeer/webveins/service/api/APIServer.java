package com.xiongbeer.webveins.service.api;


import com.xiongbeer.webveins.saver.HDFSManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.apache.zookeeper.ZooKeeper;

/**
 * Created by shaoxiong on 17-5-13.
 */
public class APIServer {
    private ZooKeeper zk;
    private HDFSManager hdfsManager;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public APIServer(ZooKeeper zk, HDFSManager hdfsManager){
        this.zk = zk;
        this.hdfsManager = hdfsManager;
    }

    public void run(int port){
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel)
                            throws Exception {
                        channel.pipeline().addLast(new LineBasedFrameDecoder(1024));
                        channel.pipeline().addLast(new StringDecoder());
                        channel.pipeline().addLast(new APIServerHandler(zk, hdfsManager));
                    }
                });
        try {
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
