package com.xiongbeer.webveins;

import com.xiongbeer.webveins.check.SelfTest;
import com.xiongbeer.webveins.exception.VeinsException;
import com.xiongbeer.webveins.filter.UrlFilter;
import com.xiongbeer.webveins.saver.HDFSManager;
import com.xiongbeer.webveins.service.balance.BalanceServer;
import com.xiongbeer.webveins.utils.IdProvider;
import com.xiongbeer.webveins.utils.InitLogger;
import com.xiongbeer.webveins.zk.manager.Manager;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 启动入口
 * Created by shaoxiong on 17-4-20.
 */
@SuppressWarnings("restriction")
public class WebVeinsMain implements Watcher{
    private static WebVeinsMain wvMain;
    private ZooKeeper zk;
    private String serverId;
    private Configuration configuration;
    private Timer managerTimer;
    private Logger logger = LoggerFactory.getLogger(WebVeinsMain.class);
    private String ip = new IdProvider().getIp();
    private BalanceServer balanceServer;
    private Manager manager;
    private HDFSManager hdfsManager;

    private WebVeinsMain() throws IOException {
    	configuration = Configuration.getInstance();
        String connectString = ip + ':'
                + Configuration.ZOOKEEPER_MANAGER_ADDRESS.get(ip);
        zk = new ZooKeeper(connectString,
                    Configuration.ZK_SESSION_TIMEOUT, this);
        serverId = ip;
        hdfsManager = new HDFSManager(Configuration.HDFS_SYSTEM_CONF
                , Configuration.HDFS_SYSTEM_PATH);

        /* 监听kill信号 */
        SignalHandler handler = new StopSignalHandler();
        Signal termSignal = new Signal("TERM");
        Signal.handle(termSignal, handler);
    }
    
    public static synchronized WebVeinsMain getInstance()
            throws IOException {
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
        manager = new Manager(zk, serverId,
                hdfsManager, filter);

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
                } catch (Throwable e){
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
                    balanceServer = new BalanceServer(
                            Configuration.BALANCE_SERVER_PORT, manager);
                    balanceServer.bind();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }.start();
    }


    @SuppressWarnings("restriction")
    private class StopSignalHandler implements SignalHandler {
        @Override
        public void handle(Signal signal) {
            try {
                logger.info("stoping manager...");
                /* 必须先purge，否则可能会在main线程退出后还运行一次 */
                managerTimer.purge();
                managerTimer.cancel();
                logger.info("stoping balance server...");
                balanceServer.stop();
                hdfsManager.close();
            } catch (Throwable e) {
                System.out.println("handle|Signal handler" + "failed, reason "
                        + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {}

    public static void main(String[] args) throws IOException {
        if(SelfTest.check(WebVeinsMain.class.getSimpleName())){
            System.out.println("[Error] Service has already running");
            System.exit(1);
        }
        InitLogger.init();
        WebVeinsMain main = WebVeinsMain.getInstance();
        main.run();
    }
}
