package com.xiongbeer.webveins.zk.task;

import com.xiongbeer.webveins.utils.Async;
import com.xiongbeer.webveins.utils.Tracker;
import com.xiongbeer.webveins.ZnodeInfo;
import org.apache.zookeeper.*;
import org.apache.zookeeper.AsyncCallback.*;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by shaoxiong on 17-4-7.
 */
public class Task {
    public static final String WAITING   = "0";
    public static final String RUNNING   = "1";
    public static final String FINISHED  = "2";

    protected ZooKeeper client;
    protected Map<String, Epoch> tasksInfo = new ConcurrentHashMap<>();
    protected Logger logger = LoggerFactory.getLogger(Task.class);

    public Task(ZooKeeper zk){
        client = zk;
    }

    @Deprecated
    private ChildrenCallback checkTasksChildrenCallback = new ChildrenCallback() {
        public void processResult(int rc, String path, Object ctx, List<String> list) {
            Tracker tracker = (Tracker)ctx;
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    checkTasks(tracker);
                    break;
                case OK:
                    /* list中存储的是children节点的name，而不是path */
                    ArrayList<String> tasks = (ArrayList<String>) list;
                    for(String task:tasks){
                        try {
                            checkTask(path + "/" + task);
                        } catch (Exception e) {
                            logger.warn("Check task: " + task + " failed.");
                            e.printStackTrace();
                        }
                    }
                    if(ctx != null) {
                        tracker.setStatus(Tracker.SUCCESS);
                    }
                    break;
                default:
                    if(ctx != null) {
                        tracker.setStatus(Tracker.FAILED);
                    }
            }
        }
    };

    @Async
    @Deprecated
    public void checkTasks(Tracker tracker){
        client.getChildren(
                ZnodeInfo.TASKS_PATH,
                false,
                checkTasksChildrenCallback,
                tracker
        );
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
                    client.getChildren(path, false);
            for(String task:tasks){
                try {
                    checkTask(path + "/" + task);
                } catch (Exception e) {
                    logger.warn("Check task: " + task + " failed.");
                    e.printStackTrace();
                }
            }
        } catch (KeeperException.ConnectionLossException e) {
            checkTasks();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
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
            String status =
                    new String(client.getData(path, false, stat));
            Epoch taskInfo = new Epoch(stat.getMtime(), status, stat.getVersion());
            tasksInfo.put(new File(path).getName(), taskInfo);
        } catch (KeeperException.ConnectionLossException e) {
            checkTask(path);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
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
