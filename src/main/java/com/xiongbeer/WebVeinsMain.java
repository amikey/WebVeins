package com.xiongbeer;

import com.xiongbeer.filter.bloom.UrlFilter;
import com.xiongbeer.zk.manager.Manager;

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
    private String serverId;
    private Configuration configuration;
    private Timer managerTimer;

    private WebVeinsMain() throws IOException {
    	configuration = Configuration.getInstance();
        zk = new ZooKeeper(Configuration.ZOOKEEPER_INIT_SERVER, 1000, this);
        serverId = new IdProvider().getId();
    }
    
    public static synchronized WebVeinsMain getInstance() throws IOException {
        if(wvMain == null){
            wvMain = new WebVeinsMain();
        }
        return wvMain;
    }
    
    public void stopManager(){
        managerTimer.cancel();
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
        long intevalPeriod = Configuration.CHECK_TIME * 1000;
        managerTimer.scheduleAtFixedRate(task, delay, intevalPeriod);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {}

    public static void main(String[] args) throws IOException {
        InitLogger.init();
        WebVeinsMain main = WebVeinsMain.getInstance();
        main.runManager();;
    }
}
