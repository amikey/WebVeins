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
    private static final Logger logger = LoggerFactory.getLogger(WebVeinsMain.class);
    private static WebVeinsMain wvMain;
    private String serverId;
    private Manager manager;
    private HDFSManager hdfsManager;
    private CuratorFramework client;
    private Configuration configuration;
    private ScheduledExecutorService manageExector = Executors.newScheduledThreadPool(1);

    private WebVeinsMain() throws IOException {
    	configuration = Configuration.getInstance();
        client = SelfTest.checkAndGetZK();
        serverId = new IdProvider().getIp();
        hdfsManager = SelfTest.checkAndGetHDFS();
        /* 监听kill信号 */
        SignalHandler handler = new StopSignalHandler();
        Signal termSignal = new Signal("TERM");
        Signal.handle(termSignal, handler);
    }

    /**
     * 定时执行manage
     */
    private void run(){
        manager = Manager.getInstance(client, serverId,
                hdfsManager, configuration.getUrlFilter());
        manageExector.scheduleAtFixedRate(() -> {
            try {
                manager.manage();
            } catch (InterruptedException e) {
                logger.info("shut down.");
                return;
            } catch (Throwable e){
                logger.warn("something wrong when managing: ", e);
            }
        }, 0, Configuration.CHECK_TIME, TimeUnit.SECONDS);
    }

    public static synchronized WebVeinsMain getInstance()
            throws IOException {
        if(wvMain == null){
            wvMain = new WebVeinsMain();
        }
        return wvMain;
    }

    private class StopSignalHandler implements SignalHandler {
        @Override
        public void handle(Signal signal) {
            try {
                logger.info("stoping manager...");
                manager.stop();
                manageExector.shutdownNow();
                client.close();
                hdfsManager.close();
            } catch (Throwable e) {
                logger.error("handle|Signal handler" + "failed, reason "
                        + e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if(SelfTest.checkRunning(WebVeinsMain.class.getSimpleName())){
            logger.error("Service has already running");
            System.exit(1);
        }
        InitLogger.init();
        WebVeinsMain main = WebVeinsMain.getInstance();
        main.run();
    }
}
