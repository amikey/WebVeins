package com.xiongbeer.webveins.zk.task;

import com.xiongbeer.webveins.ZnodeInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shaoxiong on 17-5-6.
 */
public class TaskWatcher{
    private CuratorFramework client;
    private Logger logger = LoggerFactory.getLogger(TaskWatcher.class);
    public static int WAITING_TIME = 2 * 1000;
    public TaskWatcher(CuratorFramework client){
        this.client = client;
    }

    /**
     * 在没有可领取任务时阻塞
     * 刷新频率为WAITING_TIME/次
     */
    public void waitForTask(){
        try {
            while(true) {
                List<String> children =
                        (ArrayList<String>) client.getChildren().forPath(ZnodeInfo.TASKS_PATH);
                for (String child : children) {
                    byte[] data =
                            client.getData().forPath(ZnodeInfo.NEW_TASK_PATH + child);
                    if (new String(data).equals(Task.Status.WAITING.getValue())) {
                        return;
                    }
                }
                Thread.sleep(WAITING_TIME);
            }
        } catch (Exception e) {
            logger.warn("some thing get wrong when waiting for task.", e);
        }
    }

}
