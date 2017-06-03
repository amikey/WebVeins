package com.xiongbeer.webveins.zk.task;

import com.xiongbeer.webveins.ZnodeInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by shaoxiong on 17-4-7.
 */
public class Task {
    public enum Status{
        WAITING("0"), RUNNING("1"), FINISHED("2");
        private final String value;
        Status(String value){
            this.value = value;
        }
        public String getValue(){
            return value;
        }
        public static Status get(String type){
            switch (type){
                case "0":
                    return WAITING;
                case "1":
                    return RUNNING;
                case "2":
                    return FINISHED;
                default:
                    return null;
            }
        }
    }
    protected static final Logger logger = LoggerFactory.getLogger(Task.class);
    protected CuratorFramework client;
    protected Map<String, Epoch> tasksInfo = new ConcurrentHashMap<>();

    public Task(CuratorFramework client){
        this.client = client;
    }

    /**
     * 遍历目前所有任务
     *
     * 成功后对每个task进行checkTask
     */
    public void checkTasks(){
        String path = ZnodeInfo.TASKS_PATH;
        try {
            List<String> tasks =
                    client.getChildren().forPath(path);
            for(String task:tasks){
                checkTask(path + "/" + task);
            }
        } catch (Exception e) {
            logger.warn("failed to update tasks' information", e);
        }
    }

    /**
     * 检查任务
     *
     * 记录任务上一次修改的时间
     * 检查任务是否已经完成，若
     * 完成则release它
     * @param path
     */
    public void checkTask(String path) {
        try {
            Stat stat = new Stat();
            Status status = Status.get(new String(
                    client.getData()
                    .storingStatIn(stat)
                    .forPath(path)));
            Epoch taskInfo = new Epoch(stat.getMtime(), status, stat.getVersion());
            tasksInfo.put(new File(path).getName(), taskInfo);
        } catch (Exception e) {
            logger.warn("Check task: " + path + " failed.", e);
        }
    }


    /**
     * 获取所有未完成Task的信息
     * Epoch中包含
     *   - 上一次修改的时间
     *   - 最后检查的时间
     *   - 状态
     * 信息新鲜度取决于上一次checkTasks的时间
     * @return
     */
    public Map<String, Epoch> getTasksInfo(){
        return new HashMap<>(tasksInfo);
    }
}
