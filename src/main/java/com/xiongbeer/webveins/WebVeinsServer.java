package com.xiongbeer.webveins;

import java.io.IOException;
import java.util.*;

import com.google.protobuf.InvalidProtocolBufferException;
import com.xiongbeer.webveins.check.SelfTest;
import com.xiongbeer.webveins.saver.HDFSManager;
import com.xiongbeer.webveins.service.api.APIServer;
import com.xiongbeer.webveins.service.balance.BalanceClient;
import com.xiongbeer.webveins.utils.Color;
import com.xiongbeer.webveins.utils.IdProvider;
import com.xiongbeer.webveins.utils.InitLogger;
import com.xiongbeer.webveins.zk.manager.ManagerData;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import com.xiongbeer.webveins.service.local.Server;
import com.xiongbeer.webveins.zk.worker.Worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Signal;
import sun.misc.SignalHandler;

@SuppressWarnings("restriction")
public class WebVeinsServer implements Watcher {
	private Server server;
	private Worker worker;
	private String serverId;
	private ZooKeeper zk;
	private static WebVeinsServer wvServer;
	private BalanceClient balanceClient;
	private APIServer apiServer;
	private HDFSManager hdfsManager;
    private Logger logger = LoggerFactory.getLogger(WebVeinsServer.class);

	private WebVeinsServer() throws IOException {
    	Configuration.getInstance();
        zk = SelfTest.checkAndGetZK(this);
        if(zk == null){
            logger.error("[init] Connect to zookeeper server failed.");
            System.exit(1);
        }
        hdfsManager = SelfTest.checkAndGetHDFS();
        if(hdfsManager == null){
            logger.error("[init] Connect to hdfs failed.");
            System.exit(1);
        }
        serverId = new IdProvider().getIp();
        balanceClient = new BalanceClient();
        apiServer = new APIServer(zk, hdfsManager);

        /* 监听kill信号 */
        SignalHandler handler = new StopSignalHandler();
        Signal termSignal = new Signal("TERM");
        Signal.handle(termSignal, handler);
    }
    
    public static synchronized WebVeinsServer getInstance()
            throws IOException {
        if(wvServer == null){
        	wvServer = new WebVeinsServer();
        }
        return wvServer;
    }
    
    public void stopServer(){
        server.stop();
    }
    
    public void setZK(ZooKeeper zk){
        this.zk = zk;
    }

    public void runServer() throws IOException {
        worker = new Worker(zk, serverId);
        server = new Server(Configuration.LOCAL_PORT, worker);
        server.bind();
    }

    public void connectBalanceManager()
            throws KeeperException, InterruptedException {
        ArrayList<String> children =
                (ArrayList<String>) zk.getChildren(
                        ZnodeInfo.MANAGERS_PATH, false);
        List<ManagerData> managerData = new LinkedList<ManagerData>();
        for(String child:children){
            byte[] data = zk.getData(ZnodeInfo.MANAGERS_PATH
                            + '/' + child, false, null);
            try {
                managerData.add(new ManagerData(data));
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(managerData);

        /* 拿到负载最小的Manager */
        ManagerData manager = null;
        try {
            manager = managerData.get(0);
        } catch (Throwable e){
            logger.error(Color.error("Cannot connect to manager balance server."));
            System.exit(1);
        }
        balanceClient.connect(manager, this, apiServer, hdfsManager);
    }

    public void run(){
        try {
            connectBalanceManager();
        } catch (KeeperException.ConnectionLossException e) {
            run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
	public void process(WatchedEvent arg0) {}

    @SuppressWarnings("restriction")
    private class StopSignalHandler implements SignalHandler {
        @Override
        public void handle(Signal signal) {
            try {
                logger.info("stoping server...");
                server.stop();
                logger.info("stoping balance client...");
                balanceClient.stop();
                logger.info("stoping api service...");
                apiServer.stop();
                hdfsManager.close();
            } catch (Throwable e) {
                System.out.println("handle|Signal handler" + "failed, reason "
                        + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
            throws IOException, InterruptedException {
        if(SelfTest.checkRunning(WebVeinsServer.class.getSimpleName())){
            System.out.println("[Error] Service has already running");
            System.exit(1);
        }
        InitLogger.init();
        WebVeinsServer server = WebVeinsServer.getInstance();
        server.run();
    }
}
