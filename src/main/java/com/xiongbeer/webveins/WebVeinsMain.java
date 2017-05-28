package com.xiongbeer.webveins;

import com.xiongbeer.webveins.check.SelfTest;
import com.xiongbeer.webveins.filter.UrlFilter;
import com.xiongbeer.webveins.saver.HDFSManager;
import com.xiongbeer.webveins.utils.IdProvider;
import com.xiongbeer.webveins.utils.InitLogger;
import com.xiongbeer.webveins.zk.manager.Manager;

import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 启动入口
 * Created by shaoxiong on 17-4-20.
 */
@SuppressWarnings("restriction")
public class WebVeinsMain {
    private static WebVeinsMain wvMain;
    private CuratorFramework client;
    private String serverId;
    private Configuration configuration;
    private Logger logger = LoggerFactory.getLogger(WebVeinsMain.class);
    private Manager manager;
    private HDFSManager hdfsManager;
    private ScheduledExecutorService manageExector = Executors.newScheduledThreadPool(1);
    private WebVeinsMain() throws IOException {
    	configuration = Configuration.getInstance();
        client = SelfTest.checkAndGetZK();
        if(client == null){
            logger.error("[init] Connect to zookeeper server failed.");
            System.exit(1);
        }
        hdfsManager = SelfTest.checkAndGetHDFS();
        if(hdfsManager == null){
            logger.error("[init] Connect to hdfs failed.");
            System.exit(1);
        }
        serverId = new IdProvider().getIp();
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

    /**
     * 定时执行manage
     */
    private void run(){
        UrlFilter filter = configuration.getUrlFilter();
        manager = Manager.getInstance(client, serverId,
                hdfsManager, filter);

        manageExector.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    manager.manage();
                } catch (InterruptedException e) {
                    logger.warn("shut down.");
                    return;
                } catch (Throwable e){
                    logger.warn("something wrong when managing: ", e);
                }
            }
        }, 0, Configuration.CHECK_TIME, TimeUnit.SECONDS);
    }


    @SuppressWarnings("restriction")
    private class StopSignalHandler implements SignalHandler {
        @Override
        public void handle(Signal signal) {
            try {
                logger.info("stoping manager...");
                manageExector.shutdownNow();
                client.close();
                hdfsManager.close();
            } catch (Throwable e) {
                System.out.println("handle|Signal handler" + "failed, reason "
                        + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if(SelfTest.checkRunning(WebVeinsMain.class.getSimpleName())){
            System.out.println("[Error] Service has already running");
            System.exit(1);
        }
        InitLogger.init();
        WebVeinsMain main = WebVeinsMain.getInstance();
        main.run();
    }
}
