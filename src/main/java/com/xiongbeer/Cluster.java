package com.xiongbeer;

import org.apache.zookeeper.AsyncCallback.*;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

/**
 * Created by shaoxiong on 17-4-9.
 */
public class Cluster {
    static private Cluster cluster;

    private ZooKeeper client;
    private Logger logger = LoggerFactory.getLogger(Cluster.class);

    //TODO
    private StringCallback createParentCallback = new StringCallback() {
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (Code.get(rc)) {
                case CONNECTIONLOSS:
                    break;
                case OK:
                    break;
                case NODEEXISTS:
                    break;
                default:

            }
        }
    };

    //TODO
    private VoidCallback deleteParentCallback = new VoidCallback() {
        public void processResult(int rc, String path, Object ctx) {
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    break;
                case OK:
                    break;
                default:
            }
        }
    };

    /**
     * 初始化znode基础设置，生成3个永久的znode
     * /
     * |--- wvTasks
     * |--- wvWorkers
     * |--- wvManagers
     */
    public void init(){
        createParent(ZnodeInfo.WORKERS_PATH);
        createParent(ZnodeInfo.MANAGERS_PATH);
        createParent(ZnodeInfo.TASKS_PATH);
    }

    /**
     * 强制初始化(会清除原来存在的节点)
     */
    public void format(){
        deleteParent(ZnodeInfo.WORKERS_PATH);
        deleteParent(ZnodeInfo.MANAGERS_PATH);
        deleteParent(ZnodeInfo.TASKS_PATH);
        init();
    }


    static public synchronized Cluster getInstance(ZooKeeper zk) {
        if (cluster == null) {
            cluster = new Cluster(zk);
        }
        return cluster;
    }

    private void createParent(String path){
        client.create(path,
                "".getBytes(),
                OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT,
                createParentCallback,
                null);
    }

    private void deleteParent(String path){
        client.delete(path,
                        0,
                        deleteParentCallback,
                        null);
    }

    private Cluster(ZooKeeper zk){
        client = zk;
    }
}
