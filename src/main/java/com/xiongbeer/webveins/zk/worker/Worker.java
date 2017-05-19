package com.xiongbeer.webveins.zk.worker;

import com.xiongbeer.webveins.ZnodeInfo;
import com.xiongbeer.webveins.zk.task.TaskWatcher;
import com.xiongbeer.webveins.zk.task.TaskWorker;
import org.apache.zookeeper.AsyncCallback.*;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

/**
 * Created by shaoxiong on 17-4-9.
 */
public class Worker {
    private ZooKeeper client;
    private String serverId;
    private String workerPath;
    private Logger logger = LoggerFactory.getLogger(Worker.class);
    private TaskWorker taskWorker;
    private TaskWatcher taskWatcher;

    public Worker(ZooKeeper zk, String serverId){
        client = zk;
        taskWorker = new TaskWorker(zk);
        taskWatcher = new TaskWatcher(zk);
        this.serverId = serverId;
        signUpWorker();
    }

    public void stop(){
        logger.info("Trying to stop worker." + serverId + " ...");
        try {
            client.delete(workerPath, -1);
            logger.info("Stop " + serverId + " success.");
        } catch (KeeperException.ConnectionLossException e){
          logger.warn("Connection loss, retry ...");
          stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    public TaskWorker getTaskWorker(){
        return taskWorker;
    }

    public TaskWatcher getTaskWatcher(){return taskWatcher;}

    public void resetZK(ZooKeeper client){
        this.client = client;
    }

    public void waitForTask(){
        taskWatcher.waitForTask();
    }

    public void setStatus(String taskName){
        try {
            client.setData(workerPath, taskName.getBytes(), -1);
        } catch (KeeperException.ConnectionLossException e) {
            setStatus(taskName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    public String takeTask(){
        String taskName;
        taskName = taskWorker.takeTask();
        if(taskName != null){
            setStatus(taskName);
        }
        return taskName;
    }

    public void beat(String taskName){
        taskWorker.setRunningTask(ZnodeInfo.TASKS_PATH + '/' + taskName, -1);
        setStatus(taskName);
    }

    public void discardTask(String taskPath){
        taskWorker.discardTask(taskPath);;
    }

    public void finishTask(String taskPath){
        taskWorker.finishTask(taskPath);
        setStatus("");
    }

    private StringCallback workerCreateCallback = new StringCallback() {
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    signUpWorker();
                    break;
                case OK:
                    logger.info("Worker sign up success by server." + serverId);
                    workerPath = path;
                    break;
                default:
                    logger.error("Something went wrong when sign up worker.",
                            KeeperException.create(Code.get(rc), path));
                    System.exit(1);
            }
        }
    };

    private void signUpWorker(){
        client.create(ZnodeInfo.NEW_WORKER_PATH + serverId,
                        serverId.getBytes(),
                        OPEN_ACL_UNSAFE,
                        CreateMode.EPHEMERAL,
                        workerCreateCallback,
                        null);
    }
}
