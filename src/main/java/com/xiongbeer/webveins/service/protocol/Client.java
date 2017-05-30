package com.xiongbeer.webveins.service.protocol;

import com.xiongbeer.webveins.Configuration;
import com.xiongbeer.webveins.service.local.Action;
import com.xiongbeer.webveins.service.protocol.handler.HeartBeatReqHandler;
import com.xiongbeer.webveins.service.protocol.handler.LocalCrawlerHandler;
import com.xiongbeer.webveins.service.protocol.handler.LoginAuthReqHandler;
import com.xiongbeer.webveins.service.protocol.handler.ShellReqHandler;
import com.xiongbeer.webveins.service.protocol.message.Header;
import com.xiongbeer.webveins.service.protocol.message.Message;
import com.xiongbeer.webveins.service.protocol.message.MessageDecoder;
import com.xiongbeer.webveins.service.protocol.message.MessageEncoder;
import com.xiongbeer.webveins.service.protocol.message.Message.Coderc;
import com.xiongbeer.webveins.service.ProcessDataProto.ProcessData;
import com.xiongbeer.webveins.utils.InitLogger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by shaoxiong on 17-5-28.
 */
public class Client {
    private static Channel channel;
    private static Logger logger = LoggerFactory.getLogger(Client.class);
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private AtomicBoolean isLongConnection = new AtomicBoolean(false);
    private Action action;
    private EventLoopGroup group = new NioEventLoopGroup();

    public void setAction(Action action){
        this.action = action;
    }

    public void disconnect(){
        channel.close();
    }

    public void connect(final int port, final String host, final Message initMessage) {
        /*
        if(action == null){
            logger.error("Connect failed, action is null");
            return;
        }
        */
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(new MessageDecoder(1024*1024,  4, 4, -8, 0));
                            ch.pipeline().addLast("MessageEncoder", new MessageEncoder());
                            ch.pipeline().addLast("ReadTimeoutHandler", new ReadTimeoutHandler(50));
                            ch.pipeline().addLast("LoginAuthHandler", new LoginAuthReqHandler(isLongConnection, initMessage));
                            ch.pipeline().addLast("ShellReqHandler", new ShellReqHandler());
                            ch.pipeline().addLast("LocalCrawlerHandler", new LocalCrawlerHandler(action));
                            ch.pipeline().addLast("HeartBeatHandler", new HeartBeatReqHandler(isLongConnection));
                        }
                    });
            ChannelFuture f = b.connect(host,port).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(isLongConnection.get()) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TimeUnit.SECONDS.sleep(5);
                            connect(port, host, initMessage);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        Configuration.getInstance();
        InitLogger.init();
        int port = 8080;
        Message message = new Message();
        Header header = new Header();
        header.setType(Coderc.SHELL_REQ.getValue());
        ProcessData data = ProcessData.newBuilder().setCommand("listfilters").build();
        message.setHeader(header);
        message.setBody(data);
        new Client().connect(port, "127.0.0.1", message);
    }
}
