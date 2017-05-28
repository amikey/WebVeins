package com.xiongbeer.webveins.zk;

import com.xiongbeer.webveins.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用于执行不依赖顺序性的操作，防止EventThread阻塞，提高执行效率
 *
 * Created by shaoxiong on 17-5-28.
 */
public class AsyncOpThreadPool {
    private final ExecutorService threadPool;
    private static AsyncOpThreadPool asyncOpThreadPool;
    private AsyncOpThreadPool(){
        threadPool  = Executors.newFixedThreadPool(Configuration.LOCAL_ASYNC_THREAD_NUM);
    }

    public static synchronized AsyncOpThreadPool getInstance(){
        if(asyncOpThreadPool == null){
            asyncOpThreadPool = new AsyncOpThreadPool();
        }
        return asyncOpThreadPool;
    }

    public ExecutorService getThreadPool(){
        return threadPool;
    }
}
