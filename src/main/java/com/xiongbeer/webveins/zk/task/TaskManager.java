package com.xiongbeer.webveins.zk.task;

import com.xiongbeer.webveins.utils.Async;
import com.xiongbeer.webveins.ZnodeInfo;
import com.xiongbeer.webveins.zk.AsyncOpThreadPool;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.zookeeper.*;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.data.Stat;

import java.io.File;
import java.util.concurrent.ExecutorService;

/**
 * Created by shaoxiong on 17-4-10.
 */
public class TaskManager extends Task{
    public TaskManager(CuratorFramework client) {
        super(client);
    }
    private ExecutorService asyncOpThreadPool = AsyncOpThreadPool.getInstance().getThreadPool();

    /**
     * submit的Callback函数
     *
     * 提交任务成功后不立即刷新Tasks列表
     * 是为了减轻Manager服务器的压力
     */
    private BackgroundCallback submitTaskCallback = new BackgroundCallback() {
        @Override
        public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
            int rc = curatorEvent.getResultCode();
            String name = curatorEvent.getName();
            String path = curatorEvent.getPath();
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    submit(name);
                    break;
                case OK:
                    logger.info("Submit task: " + path + " success.");
                    break;
                case NODEEXISTS:
                    // pass
                    break;
                default:
                    logger.error("Something went wrong when submit task.",
                            KeeperException.create(Code.get(rc), path));
                    break;
            }
        }
    };


    private BackgroundCallback resetTaskCallback = new BackgroundCallback() {
        @Override
        public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
            int rc = curatorEvent.getResultCode();
            String path = curatorEvent.getPath();
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
                    logger.error("Something went wrong when reset task.",
                            KeeperException.create(Code.get(rc), path));
                    break;
            }
        }
    };


    private BackgroundCallback releaseTaskCallback = new BackgroundCallback() {
        @Override
        public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
            int rc = curatorEvent.getResultCode();
            String path = curatorEvent.getPath();
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    releaseTask(path);
                    break;
                case OK:
                    String dataUrl = new File(path).getName();
                    if(tasksInfo.containsKey(dataUrl)){
                        tasksInfo.remove(dataUrl);
                    }
                    logger.info("Release task: " + dataUrl + " success.");
                    break;
                default:
                    logger.error("Something went wrong when release task.",
                            KeeperException.create(Code.get(rc), path));
                    break;
            }
        }
    };

    /**
     * 提交一个新的任务
     *
     * 节点包含数据指当前状态
     * @param name
     */
    public void submit(String name){
        try {
            TaskData taskData = new TaskData();
            taskData.setStatus(Status.WAITING);
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .inBackground(submitTaskCallback, asyncOpThreadPool)
                    .forPath(ZnodeInfo.NEW_TASK_PATH + name, taskData.getBytes());
        } catch (Exception e) {
            logger.error("unknow error", e);
        }
    }

    /**
     * 重置任务
     *
     * 任务失败(与Worker失去连接)，重置其
     * 状态，等待其他Worker重新接管任务
     * @param path
     */
    public void resetTask(String path){
        try {
            Stat stat = new Stat();
            byte[] data = client.getData()
                    .storingStatIn(stat)
                    .forPath(path);
            TaskData taskData = new TaskData(data);
            taskData.setStatus(Status.WAITING);
            client.setData()
                    .inBackground(resetTaskCallback, asyncOpThreadPool)
                    .forPath(path, taskData.getBytes());
        } catch (Exception e) {
            logger.error("unknow error", e);
        }
    }

    /**
     * 释放任务
     *
     * 任务执行成功，释放该任务节点
     * @param path
     */
    @Async
    public void releaseTask(String path){
        try {
            client.delete()
                    .inBackground(releaseTaskCallback, asyncOpThreadPool)
                    .forPath(path);
        } catch (Exception e) {
            logger.error("unknow error", e);
        }
    }
}
