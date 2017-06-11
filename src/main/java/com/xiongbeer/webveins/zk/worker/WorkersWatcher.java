package com.xiongbeer.webveins.zk.worker;

import com.xiongbeer.webveins.ZnodeInfo;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 监视workers下运行客户端的连接状态
 * Created by shaoxiong on 17-4-6.
 */
public class WorkersWatcher implements Watcher{
    private static final Logger logger = LoggerFactory.getLogger(WorkersWatcher.class);
    private Map<String, String> workersMap = new ConcurrentHashMap<>();
    private CuratorFramework client;
    
    public WorkersWatcher(CuratorFramework client){
        this.client = client;
    }

    /**
     * 获得(刷新)worker列表
     */
    public void refreshAliveWorkers(){
        try {
            List<String> children =
                    client.getChildren()
                            .usingWatcher(this)
                            .forPath(ZnodeInfo.WORKERS_PATH);
            /* 首先检查上一次保存的worker中有没有消失的 */
            workersMap.entrySet().forEach(entry -> {
                String workerName = entry.getKey();
                if(!children.contains(workerName)){
                    workersMap.remove(workerName);
                }
            });
            /* 检查是否有新的worker */
            children.forEach(workerName -> workersMap.putIfAbsent(workerName, null));
        } catch (Exception e) {
            logger.warn("failed to refresh worker status. ", e);
        }
    }

    /**
     * 刷新所有worker列表中worker的状态
     */
    public void refreshAllWorkersStatus(){
        workersMap.entrySet().forEach(entry -> refreshWorkerStatus(entry.getKey()));
    }

    public Map<String, String> getWorkersMap(){
        return workersMap;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged){
            assert ZnodeInfo.WORKERS_PATH.equals( watchedEvent.getPath() );
            refreshAliveWorkers();
        }
    }

    private void refreshWorkerStatus(String workerName){
        try {
            byte[] data = client.getData()
                    .forPath(ZnodeInfo.WORKERS_PATH + "/" + workerName);
            workersMap.put(workerName, new String(data));
        } catch (KeeperException.ConnectionLossException e) {
            refreshWorkerStatus(workerName);
        } catch (KeeperException.NoNodeException e){
            workersMap.remove(workerName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

