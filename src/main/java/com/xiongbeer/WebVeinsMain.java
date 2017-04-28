package com.xiongbeer;

import com.xiongbeer.filter.bloom.UrlFilter;
import com.xiongbeer.zk.manager.Manager;
import com.xiongbeer.service.Server;
import com.xiongbeer.zk.worker.Worker;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 启动入口
 * Created by shaoxiong on 17-4-20.
 */
public class WebVeinsMain implements Watcher{
    private static WebVeinsMain wvMain;
    private ZooKeeper zk;
    private Worker worker;
    private String serverId;
    private Configuration configuration;
    private Timer managerTimer;
    private Server server;

    private WebVeinsMain() throws IOException {
        zk = new ZooKeeper(configuration.ZOOKEEPER_INIT_SERVER, 1000, this);
        configuration = Configuration.getInstance();
        serverId = "1";
    }

    public void stopManager(){
        managerTimer.cancel();
    }

    public void stopServer(){
        server.stop();
    }

    public static synchronized WebVeinsMain getInstance() throws IOException {
        if(wvMain == null){
            wvMain = new WebVeinsMain();
        }
        return wvMain;
    }

    /**
     * 定时执行manage
     */
    private void runManager(){
        UrlFilter filter = configuration.getUrlFilter();
        final Manager manager = new Manager(zk, serverId,
                Configuration.HDFS_SYSTEM_PATH, filter);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    manager.manage();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (VeinsException.FilterOverflowException e) {
                    e.printStackTrace();
                }
            }
        };
        managerTimer = new Timer();
        long delay = 0;
        long intevalPeriod = 10 * 1000;
        managerTimer.scheduleAtFixedRate(task, delay, intevalPeriod);
    }

    private void runServer() throws IOException {
        server = new Server(Configuration.LOCAL_HOST,
                Configuration.LOCAL_PORT, worker.getTaskWorker());
        server.bind();
    }

    private void run(String arg) throws IOException {
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
        InitLogger.init();
        WebVeinsMain main = WebVeinsMain.getInstance();
        main.run(args[0]);
    }
}
