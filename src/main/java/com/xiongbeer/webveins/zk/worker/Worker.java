package com.xiongbeer.webveins.zk.worker;

import com.xiongbeer.webveins.ZnodeInfo;
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
    public enum Status{
        Initializing, Working, STOPED
    }
    private ZooKeeper client;
    private String serverId;
    private String workerPath;
    private Logger logger = LoggerFactory.getLogger(Worker.class);
    private TaskWorker taskWorker;
    private Status status;

    public Worker(ZooKeeper zk, String serverId){
        status = Status.Initializing;
        client = zk;
        taskWorker = new TaskWorker(zk);
        this.serverId = serverId;
        signUpWorker();
    }

    public void stop(){
        logger.info("Trying to stop worker." + serverId + " ...");
        try {
            client.delete(workerPath, -1);
            status = Status.STOPED;
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

    public Status getStatus(){
        return status;
    }

    public TaskWorker getTaskWorker(){
        return taskWorker;
    }

    public void resetZK(ZooKeeper client){
        this.client = client;
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
                    status = Status.Working;
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
