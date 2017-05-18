package com.xiongbeer.webveins.service.balance;

import com.xiongbeer.webveins.Configuration;
import com.xiongbeer.webveins.WebVeinsServer;
import com.xiongbeer.webveins.saver.HDFSManager;
import com.xiongbeer.webveins.service.api.APIServer;
import com.xiongbeer.webveins.zk.manager.ManagerData;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by shaoxiong on 17-5-2.
 */
public class BalanceClient {
    private static Channel channel;
    private EventLoopGroup group;
    private HDFSManager hdfsManager;
    public void connect(ManagerData managerData, WebVeinsServer wvServer
            , APIServer apiServer, HDFSManager hdfsManager) {
        String host = managerData.getIp();
        int port = managerData.getPort();
        group = new NioEventLoopGroup();
        try {
            io.netty.bootstrap.Bootstrap b = new io.netty.bootstrap.Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new BalanceClientHandler(managerData, apiServer, wvServer, hdfsManager));
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        group.shutdownGracefully();
    }

    public void disconnect(){
        channel.close();
    }
}
