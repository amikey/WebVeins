package com.xiongbeer.webveins.zk.task;

import com.xiongbeer.webveins.ZnodeInfo;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.util.ArrayList;

/**
 * Created by shaoxiong on 17-5-6.
 */
public class TaskWatcher{
    private ZooKeeper zk;
    public TaskWatcher(ZooKeeper zk){
        this.zk = zk;
    }

    /**
     * 在没有可领取任务时阻塞
     * 刷新频率为2秒一次
     */
    public void waitForTask(){
        try {
            while(true) {
                ArrayList<String> children =
                        (ArrayList<String>) zk.getChildren(ZnodeInfo.TASKS_PATH, false);
                for (String child : children) {
                    byte[] data = zk.getData(ZnodeInfo.NEW_TASK_PATH+child,
                            false, null);
                    if (new String(data).equals(Task.WAITING)) {
                        return;
                    }
                }
                Thread.sleep(2 * 1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

}
