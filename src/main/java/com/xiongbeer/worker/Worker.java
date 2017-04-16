package com.xiongbeer.worker;

import com.xiongbeer.ZnodeInfo;
import com.xiongbeer.task.TaskWorker;
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
    private Logger logger = LoggerFactory.getLogger(Worker.class);
    private TaskWorker taskWorker;

    public Worker(ZooKeeper zk, String serverId){
        client = zk;
        this.serverId = serverId;
        taskWorker = new TaskWorker(zk);
        signUpWorker();
    }

    public void setServerId(String serverId){
        this.serverId = serverId;
    }


    private StringCallback workerCreateCallback = new StringCallback() {
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    signUpWorker();
                    break;
                case OK:
                    logger.info("Worker sign up success by server." + serverId);
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
