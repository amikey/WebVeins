package com.xiongbeer.task;

import com.xiongbeer.Async;
import com.xiongbeer.Tracker;
import com.xiongbeer.ZnodeInfo;
import org.apache.zookeeper.*;
import org.apache.zookeeper.AsyncCallback.*;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

/**
 * Created by shaoxiong on 17-4-10.
 * TODO:添加各个Callback函数default的logger info
 */
public class TaskManager extends Task{
    public TaskManager(ZooKeeper zk) {
        super(zk);
    }

    /**
     * submit的Callback函数
     *
     * 提交任务成功后不立即刷新Tasks列表
     * 是为了减轻Manager服务器的压力
     */
    private StringCallback submitTaskCallback = new StringCallback() {
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    submit(name);
                    break;
                case OK:
                    logger.info("Submit task: " + path + " success.");
                    break;
                case NODEEXISTS:
                    logger.info("Task: " + path + " has already exist.");
                    break;
                default:

            }
        }
    };

    private StatCallback resetStatCallback = new StatCallback() {
        public void processResult(int rc, String path, Object ctx, Stat stat) {
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    resetTask(path);
                    break;
                case OK:
                    logger.info("Task: " + path + " has been reset.");
                    break;
                case NONODE:
                    logger.warn("Task: " + path + " doesn't exist.");
                    break;
                default:

            }
        }
    };


    private VoidCallback releaseVoidCallback = new VoidCallback() {
        public void processResult(int rc, String path, Object ctx) {
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    releaseTask(path);
                    break;
                case OK:
                    String dataUrl = getDataName(path);
                    if(tasksInfo.containsKey(dataUrl)){
                        tasksInfo.remove(dataUrl);
                    }
                    logger.info("Release task: " + dataUrl + " success.");
                    break;
                default:
            }
        }
    };

    /**
     * 提交任务
     *
     * 节点包含数据指当前状态
     * @param name
     */
    public void submit(String name){
        client.create(
                ZnodeInfo.NEW_TASK_PATH+name,
                WAITING.getBytes(),
                OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT,
                submitTaskCallback,
                null
        );
    }

    /**
     * 重置任务
     *
     * 任务失败，重置其状态，等待
     * 其他Worker重新接管任务
     * @param path
     */
    @Async
    public void resetTask(String path){
        client.setData(
                path,
                WAITING.getBytes(),
                -1,
                resetStatCallback,
                null
        );
    }

    /**
     * 释放任务
     *
     * 任务执行成功，释放该任务节点
     * @param path
     */
    @Async
    public void releaseTask(String path){
        client.delete(
                path,
                -1,
                releaseVoidCallback,
                null
        );
    }
}
