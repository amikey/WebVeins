package com.xiongbeer.webveins.check;

import com.xiongbeer.webveins.Configuration;
import com.xiongbeer.webveins.ZnodeInfo;
import com.xiongbeer.webveins.saver.HDFSManager;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by shaoxiong on 17-5-6.
 */
public class SelfTest {

    /**
     * 检查某个class是否已经在运行
     *
     * @param className
     * @return
     */
    public static boolean checkRunning(String className){
        boolean result = false;
        int counter = 0;
        try {
            Process process = Runtime.getRuntime().exec("jps");
            InputStreamReader iR = new InputStreamReader(process.getInputStream());
            BufferedReader input = new BufferedReader(iR);
            String line;
            while((line = input.readLine()) != null){
                if(line.matches(".*"+className)){
                    counter++;
                    if(counter > 1) {
                        result = true;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 检查ZooKeeper的连接状态和它的Znode目录树
     * @param
     * @return
     */
    public static ZooKeeper checkAndGetZK(Watcher watcher) {
        ZooKeeper zk;
        try {
            zk = new ZooKeeper(Configuration.ZK_CONNECT_STRING, Configuration.ZK_SESSION_TIMEOUT, watcher);
            zk.exists(ZnodeInfo.TASKS_PATH, false);
            zk.exists(ZnodeInfo.MANAGERS_PATH, false);
            zk.exists(ZnodeInfo.WORKERS_PATH, false);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
        return zk;
    }

    /**
     * 检查HDFS的连接状态和它的目录树
     *
     * @return
     */
    public static HDFSManager checkAndGetHDFS(){
        HDFSManager hdfsManager;
        try{
            hdfsManager = new HDFSManager(Configuration.HDFS_SYSTEM_CONF, Configuration.HDFS_SYSTEM_PATH);
            hdfsManager.exist(Configuration.BLOOM_BACKUP_PATH);
            hdfsManager.exist(Configuration.FINISHED_TASKS_URLS);
            hdfsManager.exist(Configuration.WAITING_TASKS_URLS);
            hdfsManager.exist(Configuration.NEW_TASKS_URLS);
        } catch (Throwable e){
            e.printStackTrace();
            return null;
        }
        return hdfsManager;
    }
}
