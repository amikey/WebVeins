package com.xiongbeer.webveins;

import com.xiongbeer.webveins.check.SelfTest;
import com.xiongbeer.webveins.zk.task.TaskData;
import org.apache.curator.framework.CuratorFramework;

/**
 * Created by shaoxiong on 17-4-9.
 */
public class Test {

    public static void main(String[] args){
        Configuration.getInstance();
        CuratorFramework client = SelfTest.checkAndGetZK();
        TaskData data = new TaskData();
        try {
            for(String path:client.getChildren().forPath(ZnodeInfo.TASKS_PATH)){
                //client.setData().forPath(ZnodeInfo.NEW_TASK_PATH+path, data.getBytes());
                byte[] b = client.getData().forPath(ZnodeInfo.NEW_TASK_PATH+path);
                System.out.println(path + ":" + new TaskData(b));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
