package com.xiongbeer.webveins.service;

import com.xiongbeer.webveins.WebVeinsServer;
import com.xiongbeer.webveins.zk.manager.ManagerData;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.zookeeper.ZooKeeper;

/**
 * Created by shaoxiong on 17-5-2.
 */
@ChannelHandler.Sharable
public class BalanceClientHandler extends ChannelInboundHandlerAdapter {
    private ManagerData managerData;
    private WebVeinsServer wvServer;
    public BalanceClientHandler(ManagerData managerData, WebVeinsServer wvServer){
        this.managerData = managerData;
        this.wvServer = wvServer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ZooKeeper zk = new ZooKeeper(managerData.getZKConnectString(), 2000,wvServer);
        wvServer.setZK(zk);
        wvServer.runServer();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        wvServer.stopServer();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
