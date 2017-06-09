package com.xiongbeer.webveins.service.protocol;

import com.xiongbeer.webveins.Configuration;
import com.xiongbeer.webveins.service.local.Action;
import com.xiongbeer.webveins.service.protocol.handler.HeartBeatReqHandler;
import com.xiongbeer.webveins.service.protocol.handler.LocalCrawlerHandler;
import com.xiongbeer.webveins.service.protocol.handler.LoginAuthReqHandler;
import com.xiongbeer.webveins.service.protocol.handler.ShellReqHandler;
import com.xiongbeer.webveins.service.protocol.message.ProcessDataProto.ProcessData;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by shaoxiong on 17-5-28.
 */
public class Client {
    private final static Logger logger = LoggerFactory.getLogger(Client.class);
    private final String host = Configuration.LOCAL_HOST;
    private final int port = Configuration.LOCAL_PORT;
    private final Action action;
    private AtomicBoolean closeLongConnection = new AtomicBoolean(false);
    private volatile boolean isLongConnection = false;
    private EventLoopGroup group = new NioEventLoopGroup();
    private Channel[] channel = new Channel[1];
    private ChannelInitializer<SocketChannel> channelInitializer;


    /**
     * Shell查询服务，短连接
     *
     * @param command
     */
    public Client(@Nonnull final ProcessData command){
        action = null;
        channelInitializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                ch.pipeline().addLast(new ProtobufDecoder(ProcessData.getDefaultInstance()));
                ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                ch.pipeline().addLast(new ProtobufEncoder());
                ch.pipeline().addLast(new LoginAuthReqHandler(command));
                ch.pipeline().addLast(new ShellReqHandler());
            }
        };
    }


    /**
     * 本地爬虫服务，长连接
     *
     * @param action
     */
    public Client(@Nonnull final Action action){
        isLongConnection = true;
        final Client self = this;
        this.action = action;
        channelInitializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                ch.pipeline().addLast(new ProtobufDecoder(ProcessData.getDefaultInstance()));
                ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                ch.pipeline().addLast(new ProtobufEncoder());
                ch.pipeline().addLast(new ReadTimeoutHandler(60));
                ch.pipeline().addLast(new LoginAuthReqHandler(channel));
                ch.pipeline().addLast(new LocalCrawlerHandler(action));
                ch.pipeline().addLast(new HeartBeatReqHandler(self, closeLongConnection));
            }
        };
    }

    public void disconnect() {
        closeLongConnection.set(true);
        try {
            group.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        if(isLongConnection){
            longConnection();
        } else {
            shortConnection();
        }
    }

    private void longConnection(){
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(channelInitializer)
                    .connect(host, port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e){
            if(channel[0] == null){
                e.printStackTrace();
                disconnect();
            } else {
                throw e;
            }
        }
    }

    private void shortConnection(){
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(channelInitializer);
            ChannelFuture future = b.connect(host, port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }


    public void sendData(ProcessData data){
        if(channel[0] != null) {
            channel[0].writeAndFlush(data);
        }
    }

    public Action getAction(){
        return action;
    }
}
