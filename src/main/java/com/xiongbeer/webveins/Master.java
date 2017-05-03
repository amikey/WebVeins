package com.xiongbeer.webveins;

import com.xiongbeer.webveins.filter.bloom.UrlFilter;
import com.xiongbeer.webveins.service.BalanceServer;
import com.xiongbeer.webveins.utils.InitLogger;
import com.xiongbeer.webveins.zk.manager.Manager;
import org.apache.zookeeper.*;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

/**
 * Created by shaoxiong on 17-4-6.
 */
public class Master implements Watcher {
    ZooKeeper zk;
    String hostPort;

    Master(String hostPort){
        this.hostPort = hostPort;
    }

    public ZooKeeper getZK(){
        return zk;
    }


    void startZK() throws Exception{
        zk = new ZooKeeper(hostPort, 2000, this);
    }

    String queueCommand(String command) throws KeeperException{
        while(true){
            try{
                String name = zk.create("/tasks/task-",
                                        command.getBytes(),
                                        OPEN_ACL_UNSAFE,
                                        CreateMode.EPHEMERAL_SEQUENTIAL);
                return name;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void process(WatchedEvent watchedEvent) {
        System.out.println(watchedEvent);
        System.out.println(zk.getState());
    }

    public static void main(String[] args) throws Exception {

        InitLogger.init();
        Configuration.getInstance();
        Master a = new Master("192.168.0.115:2181");
        //Master b = new Master("127.0.0.1:2182");
        //Master c = new Master("127.0.0.1:2183");
        a.startZK();
        //b.startZK();
        //c.startZK();
        UrlFilter filter = new UrlFilter(500000, 0.000001, UrlFilter.CreateMode.RAM);
        Manager manager = new Manager(a.getZK(), "1","hdfs://localhost:9000/", filter);
        //new Manager(b.getZK(), "2","hdfs://localhost:9000/",filter);
        //new Manager(c.getZK(), "3","hdfs://localhost:9000/",filter);
        //manager.manage();
        //Thread.sleep(15000);
        //manager.manage();

        BalanceServer server = new BalanceServer("localhost", 8080, manager);
        server.bind();
        Thread.sleep(60000);

    }
}
