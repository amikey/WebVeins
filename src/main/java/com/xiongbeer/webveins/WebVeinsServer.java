package com.xiongbeer.webveins;

import java.io.IOException;
import java.util.*;

import com.google.protobuf.InvalidProtocolBufferException;
import com.xiongbeer.webveins.service.BalanceClient;
import com.xiongbeer.webveins.utils.IdProvider;
import com.xiongbeer.webveins.utils.InitLogger;
import com.xiongbeer.webveins.utils.Tracker;
import com.xiongbeer.webveins.zk.manager.ManagerData;
import com.xiongbeer.webveins.zk.task.TaskWatcher;
import org.apache.zookeeper.AsyncCallback.VoidCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import com.xiongbeer.webveins.service.Server;
import com.xiongbeer.webveins.zk.worker.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebVeinsServer implements Watcher {
	private Server server;
	private Worker worker;
	private String serverId;
	private ZooKeeper zk;
	private Configuration configuration;
	private static WebVeinsServer wvServer;
	private BalanceClient balanceClient;
    private Logger logger = LoggerFactory.getLogger(WebVeinsServer.class);
	private WebVeinsServer() throws IOException {
    	configuration = Configuration.getInstance();
        zk = new ZooKeeper(Configuration.INIT_SERVER, 1000, this);
        serverId = new IdProvider().getIp();
        balanceClient = new BalanceClient();
    }
    
    public static synchronized WebVeinsServer getInstance() throws IOException {
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
        server = new Server(Configuration.LOCAL_PORT,
                worker.getTaskWorker(), new TaskWatcher(zk));
        server.bind();
    }

    public void connectBalanceManager() throws KeeperException, InterruptedException {
        ArrayList<String> children =
                (ArrayList<String>) zk.getChildren(ZnodeInfo.MANAGERS_PATH, false);
        List<ManagerData> managerData = new LinkedList<ManagerData>();
        for(String child:children){
            byte[] data = zk.getData(ZnodeInfo.MANAGERS_PATH + '/' + child,
                    false, null);
            try {
                managerData.add(new ManagerData(data));
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(managerData);

        /* 拿到负载最小的Manager */
        ManagerData manager = managerData.get(0);

        balanceClient.connect(manager, this);
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
    
    public static void main(String[] args) throws IOException, InterruptedException {
        if(SelfTest.check(WebVeinsServer.class.getSimpleName())){
            System.out.println("[Error] Service has already running");
            System.exit(1);
        }
        InitLogger.init();
        WebVeinsServer server = WebVeinsServer.getInstance();
        server.run();
    }
}
