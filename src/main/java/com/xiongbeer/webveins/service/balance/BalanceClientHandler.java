package com.xiongbeer.webveins.service.balance;

import com.xiongbeer.webveins.Configuration;
import com.xiongbeer.webveins.WebVeinsServer;
import com.xiongbeer.webveins.service.api.APIServer;
import com.xiongbeer.webveins.zk.manager.ManagerData;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by shaoxiong on 17-5-2.
 */
@ChannelHandler.Sharable
public class BalanceClientHandler extends ChannelInboundHandlerAdapter {
    private ManagerData managerData;
    private WebVeinsServer wvServer;
    private Logger logger = LoggerFactory.getLogger(BalanceClientHandler.class);
    public BalanceClientHandler(ManagerData managerData, WebVeinsServer wvServer){
        this.managerData = managerData;
        this.wvServer = wvServer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String connectString = managerData.getZKConnectString();
        final ZooKeeper zk = new ZooKeeper(connectString,
                Configuration.ZK_SESSION_TIMEOUT, wvServer);
        wvServer.setZK(zk);
        logger.info("[ZK Server] Connect to " + connectString);
        /* 主服务 */
        new Thread("wvLocalServer"){
            @Override
            public void run(){
                try {
                    logger.info("run local server");
                    wvServer.runServer();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }.start();

        /* 本地命令行服务 */
        Thread apiService = new Thread("apiService"){
            @Override
            public void run() {
                APIServer apiServer = new APIServer(zk);
                apiServer.run(Configuration.LOCAL_SHELL_PORT);
            }
        };
        apiService.setDaemon(true);
        apiService.start();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        wvServer.stopServer();
        System.out.println("log out");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
