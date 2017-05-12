package com.xiongbeer.webveins.service.balance;

import com.xiongbeer.webveins.WebVeinsServer;
import com.xiongbeer.webveins.zk.manager.ManagerData;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by shaoxiong on 17-5-2.
 */
public class BalanceClient {
    private static Channel channel;
    public void connect(ManagerData managerData, WebVeinsServer wvServer) {
        String host = managerData.getIp();
        int port = managerData.getPort();

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            io.netty.bootstrap.Bootstrap b = new io.netty.bootstrap.Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new BalanceClientHandler(managerData, wvServer));
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
}
