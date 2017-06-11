package com.xiongbeer.webveins.zk.task;

import com.xiongbeer.webveins.ZnodeInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
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

        try {
            client.getChildren()
                    .forPath(ZnodeInfo.TASKS_PATH)
                    .forEach(task -> checkTask(ZnodeInfo.NEW_TASK_PATH + task));
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
            String taskName = new File(path).getName();
            Stat stat = new Stat();
            byte[] data = client.getData()
                    .storingStatIn(stat)
                    .forPath(path);
            TaskData taskData = new TaskData(data);
            Epoch taskInfo = new Epoch(taskName, stat.getMtime(), stat.getVersion(), taskData);
            tasksInfo.put(taskName, taskInfo);
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
