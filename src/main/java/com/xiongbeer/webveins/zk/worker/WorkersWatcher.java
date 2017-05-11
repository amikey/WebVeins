package com.xiongbeer.webveins.zk.worker;

import com.xiongbeer.webveins.utils.Async;
import com.xiongbeer.webveins.utils.Tracker;
import com.xiongbeer.webveins.ZnodeInfo;

import org.apache.zookeeper.*;
import org.apache.zookeeper.AsyncCallback.*;
import org.apache.zookeeper.KeeperException.Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;


/**
 * 监视workers下运行客户端的连接状态
 * Created by shaoxiong on 17-4-6.
 */
public class WorkersWatcher implements Watcher{
    private ZooKeeper client;
    private HashMap<String, String> workersList = new HashMap<String, String>();
    private Logger logger = LoggerFactory.getLogger(WorkersWatcher.class);

    @Deprecated
    private ChildrenCallback workersGetChildrenCallback = new ChildrenCallback() {
        public void processResult(int rc, String path, Object ctx, List<String> children) {
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    getWorkers((Tracker) ctx);
                    break;
                case OK:
                    logger.info("Succesfully got a list of workers: "
                                        + children.size()
                                        + " workers");
                    Iterator<Entry<String, String>> iterator = workersList.entrySet().iterator();
                    while(iterator.hasNext()){
                        @SuppressWarnings("rawtypes")
						Map.Entry entry = (Map.Entry) iterator.next();
                        if(!children.contains(entry.getKey())){
                            workersList.remove(entry.getKey());
                        }
                    }
                    for(String name:children){
                        if(!workersList.containsKey(name)){
                            workersList.put(name, null);
                        }
                    }
                    if(ctx != null) {
                        ((Tracker) ctx).setStatus(Tracker.SUCCESS);
                    }
                    break;
                default:
                   logger.warn("getChildren failed");
            }
        }
    };

    @Async
    @Deprecated
    public void getWorkers(Tracker tracker){
        client.getChildren(
                ZnodeInfo.WORKERS_PATH,
                this,
                workersGetChildrenCallback,
                tracker
        );
    }

    /**
     * 获得(刷新)worker列表
     */
    public void getWorkers(){
        try {
            List<String> children = client.getChildren(ZnodeInfo.WORKERS_PATH, this);
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
        } catch (KeeperException.ConnectionLossException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    public WorkersWatcher(ZooKeeper zk){
        this.client = zk;
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
            byte[] data = client.getData(
                    ZnodeInfo.WORKERS_PATH + "/" + workerName,
                    false,
                    null
            );
            workersList.put(workerName, new String(data));
        } catch (KeeperException.ConnectionLossException e) {
            getWorkerStatus(workerName);
        } catch (KeeperException.NoNodeException e){
            workersList.remove(workerName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged){
            assert ZnodeInfo.WORKERS_PATH.equals( watchedEvent.getPath() );
            getWorkers();
            System.out.println(watchedEvent.getType());
        }
    }

    public HashMap<String, String> getWorkersList(){
        return workersList;
    }
}

