package com.xiongbeer.webveins;

import com.xiongbeer.webveins.filter.bloom.UrlFilter;
import com.xiongbeer.webveins.service.BalanceServer;
import com.xiongbeer.webveins.utils.IdProvider;
import com.xiongbeer.webveins.utils.InitLogger;
import com.xiongbeer.webveins.zk.manager.Manager;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Logger logger = LoggerFactory.getLogger(WebVeinsMain.class);
    private String ip = new IdProvider().getIp();
    private BalanceServer balanceServer;
    private WebVeinsMain() throws IOException {
    	configuration = Configuration.getInstance();

        /* 检查本机ip是否与zk的ip匹配 */
        if(!Configuration.ZOOKEEPER_MANAGER_ADDRESS
                .containsKey(ip)){
            throw new RuntimeException("Manager ip is invaild");
        }

        String connectString = ip + ':'
                + Configuration.ZOOKEEPER_MANAGER_ADDRESS.get(ip);
        zk = new ZooKeeper(connectString, 1000, this);

        serverId = ip;
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
    private void run(){
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

        /* 均衡负载服务 */
        new Thread("BalanceServer"){
            @Override
            public void run(){
                try {
                    balanceServer = new BalanceServer(Configuration.BALANCE_SERVER_PORT, manager);
                    balanceServer.bind();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }.start();
    }



    @Override
    public void process(WatchedEvent watchedEvent) {}

    public static void main(String[] args) throws IOException {
        InitLogger.init();
        WebVeinsMain main = WebVeinsMain.getInstance();
        main.run();;
    }
}
