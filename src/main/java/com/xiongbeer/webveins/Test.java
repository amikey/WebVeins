package com.xiongbeer.webveins;


import com.alibaba.fastjson.JSON;
import com.google.common.io.Files;
import com.xiongbeer.webveins.api.info.TaskInfo;
import com.xiongbeer.webveins.api.jsondata.JData;
import com.xiongbeer.webveins.api.jsondata.TaskJson;
import com.xiongbeer.webveins.utils.InitLogger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.File;
import java.io.IOException;

/**
 * Created by shaoxiong on 17-4-9.
 */
public class Test implements Watcher{

    public static void main(String[] args) throws IOException, InterruptedException {
        InitLogger.init();
        Test test = new Test();
        ZooKeeper zooKeeper = new ZooKeeper("127.0.0.1:2181", 2000, test);
        TaskInfo taskInfo = new TaskInfo(zooKeeper);
        for(JData json:taskInfo.getCurrentTasks().getInfo()){
            System.out.println(JSON.toJSON(json));
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {

    }
}
