package com.xiongbeer;

import com.xiongbeer.filter.bloom.UrlFilter;
import com.xiongbeer.zk.manager.Manager;
import com.xiongbeer.service.Server;
import com.xiongbeer.zk.worker.Worker;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * 启动入口
 * Created by shaoxiong on 17-4-20.
 */
public class WebVeinsMain implements Watcher{
    private static WebVeinsMain wvMain;
    private ZooKeeper zk;
    private Worker worker;
    private String serverId;

    private WebVeinsMain() throws IOException {
        zk = new ZooKeeper("127.0.0.1:2181", 1000, this);
        serverId = "1";
    }

    public static synchronized WebVeinsMain getInstance() throws IOException {
        if(wvMain == null){
            wvMain = new WebVeinsMain();
        }
        return wvMain;
    }

    public void runManager(){
        UrlFilter filter = Configuration.getInstance().getUrlFilter();
        Manager manager = new Manager(zk, serverId,
                "hdfs://localhost:9000/", filter);
    }

    public void runServer() throws IOException {
        Server server = new Server(Configuration.LOCAL_HOST,
                Configuration.LOCAL_PORT, worker.getTaskWorker());
    }

    public void run(String arg) throws IOException {
        if(arg.equals("manager")){
            runManager();
        }
        else if(arg.equals("server")){
            worker = new Worker(zk, serverId);
            runServer();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {}

    public static void main(String[] args) throws IOException {
        if(args.length == 0){
            System.out.println("Error:miss arg");
            return;
        }
        //ZooKeeper zooKeeper = new ZooKeeper("127.0.0.1:2181", 1000, )
        WebVeinsMain main = WebVeinsMain.getInstance();
        main.run(args[0]);
    }
}
