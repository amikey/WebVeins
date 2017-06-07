package com.xiongbeer.webveins.zk.worker;

import com.xiongbeer.webveins.ZnodeInfo;
import com.xiongbeer.webveins.exception.VeinsException;
import com.xiongbeer.webveins.zk.task.Epoch;
import com.xiongbeer.webveins.zk.task.TaskData;
import com.xiongbeer.webveins.zk.task.TaskWatcher;
import com.xiongbeer.webveins.zk.task.TaskWorker;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by shaoxiong on 17-4-9.
 */
public class Worker {
    private static final Logger logger = LoggerFactory.getLogger(Worker.class);
    private CuratorFramework client;
    private String serverId;
    private String workerPath;
    private TaskWorker taskWorker;
    private TaskWatcher taskWatcher;

    public Worker(CuratorFramework client, String serverId){
        this.client = client;
        taskWorker = new TaskWorker(client);
        taskWatcher = new TaskWatcher(client);
        this.serverId = serverId;
        signUpWorker();
    }

    public static void addToBlackList(String taskName){
        TaskWorker.addToBlackList(taskName);
    }

    public static void clearBlackList(){
        TaskWorker.clearTaskBlackList();
    }

    public TaskWorker getTaskWorker(){
        return taskWorker;
    }

    public TaskWatcher getTaskWatcher(){return taskWatcher;}

    public String getWorkerPath(){
        return workerPath;
    }

    public void waitForTask(){
        taskWatcher.waitForTask();
    }

    public void setStatus(String taskName){
        try {
            client.setData().forPath(workerPath, taskName.getBytes());
        } catch (Exception e) {
            logger.warn("failed to set task.", e);
        }
    }

    public Epoch takeTask(){
        Epoch task;
        task = taskWorker.takeTask();
        if(task != null){
            setStatus(task.getTaskName());
        }
        return task;
    }

    public void beat(String taskName, TaskData taskData){
        taskWorker.setRunningTask(ZnodeInfo.TASKS_PATH + '/' + taskName, -1, taskData);
        setStatus(taskName);
    }

    public void discardTask(String taskPath){
        taskWorker.discardTask(taskPath);;
    }

    public void finishTask(String taskPath){
        taskWorker.finishTask(taskPath);
        setStatus("");
    }

    private void signUpWorker(){
        try {
            workerPath = client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(ZnodeInfo.NEW_WORKER_PATH + serverId, serverId.getBytes());
        } catch (KeeperException.ConnectionLossException e) {
            signUpWorker();
        } catch (Exception e) {
            throw new VeinsException.OperationFailedException("\nfailed to sign up worker. " + e.getMessage());
        }
    }
}
