package com.xiongbeer.webveins.zk.worker;

import com.xiongbeer.webveins.ZnodeInfo;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;


/**
 * 监视workers下运行客户端的连接状态
 * Created by shaoxiong on 17-4-6.
 */
public class WorkersWatcher implements Watcher{
    private CuratorFramework client;
    private HashMap<String, String> workersList = new HashMap<String, String>();
    private Logger logger = LoggerFactory.getLogger(WorkersWatcher.class);

    public WorkersWatcher(CuratorFramework client){
        this.client = client;
    }

    /**
     * 获得(刷新)worker列表
     */
    public void getWorkers(){
        try {
            List<String> children =
                    client.getChildren()
                            .usingWatcher(this)
                            .forPath(ZnodeInfo.WORKERS_PATH);
            /* 首先检查上一次保存的worker中有没有消失的 */
            Iterator<Entry<String, String>> iterator = workersList.entrySet().iterator();
            while(iterator.hasNext()){
                @SuppressWarnings("rawtypes")
                Map.Entry entry = (Map.Entry) iterator.next();
                if(!children.contains(entry.getKey())){
                    workersList.remove(entry.getKey());
                }
                /* 检查是否有新的worker */
                for(String name:children){
                    if(!workersList.containsKey(name)){
                        workersList.put(name, null);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("failed to refresh worker status.", e);
        }
    }

    /**
     * 刷新所有worker列表中worker的状态
     */
    public void reflushWorkerStatus(){
        Iterator<Entry<String, String>> iterator = workersList.entrySet().iterator();
        while(iterator.hasNext()){
            @SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry) iterator.next();
            getWorkerStatus((String) entry.getKey());
        }
    }

    private void getWorkerStatus(String workerName){
        try {
            byte[] data =
                    client.getData().forPath(ZnodeInfo.WORKERS_PATH + "/" + workerName);
            workersList.put(workerName, new String(data));
        } catch (KeeperException.ConnectionLossException e) {
            getWorkerStatus(workerName);
        } catch (KeeperException.NoNodeException e){
            workersList.remove(workerName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged){
            assert ZnodeInfo.WORKERS_PATH.equals( watchedEvent.getPath() );
            getWorkers();
        }
    }

    public HashMap<String, String> getWorkersList(){
        return workersList;
    }
}

