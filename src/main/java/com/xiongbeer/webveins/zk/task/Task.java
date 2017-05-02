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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by shaoxiong on 17-4-7.
 */
public class Task {
    public static final String WAITING   = "0";
    public static final String RUNNING   = "1";
    public static final String FINISHED  = "2";

    protected ZooKeeper client;
    protected HashMap<String, Epoch> tasksInfo = new HashMap<String, Epoch>();
    protected Logger logger = LoggerFactory.getLogger(Task.class);

    public Task(ZooKeeper zk){
        client = zk;
    }

    private ChildrenCallback checkTasksChildrenCallback = new ChildrenCallback() {
        public void processResult(int rc, String path, Object ctx, List<String> list) {
            Tracker tracker = (Tracker)ctx;
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    checkTasks(tracker);
                    break;
                case OK:
                    System.out.println(tracker);
                    /* list中存储的是children节点的name，而不是path */
                    ArrayList<String> tasks = (ArrayList<String>) list;
                    Iterator<String> iterator = tasks.iterator();
                    while(iterator.hasNext()){
                        String name = iterator.next();
                        try {
                            checkTask(path + "/" + name);
                        } catch (Exception e) {
                            logger.warn("Check task: " + name + " failed.");
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

    private DataCallback checkTaskDataCallback = new DataCallback() {
        public void processResult(int rc, String path, Object ctx, byte[] bytes, Stat stat) {
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    checkTask(path);
                    break;
                case OK:
                    String status = new String(bytes);
                    Epoch taskInfo = new Epoch(stat.getMtime(), status, stat.getVersion());
                    tasksInfo.put(getDataName(path), taskInfo);
                    break;
                default:
                    logger.warn("Check task: " + path + " failed.");
                    break;
            }
        }
    } ;

    /**
     * 遍历目前所有任务
     *
     * 成功后对每个task进行checkTask
     */
    @Async
    public void checkTasks(Tracker tracker){
        client.getChildren(
                ZnodeInfo.TASKS_PATH,
                false,
                checkTasksChildrenCallback,
                tracker
        );
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
            tasksInfo.put(getDataName(path), taskInfo);
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
    public HashMap<String, Epoch> getTasksInfo(){
        return tasksInfo;
    }


    /**
     * 提取path中的Data的Name
     *
     * @param path
     * @return
     */
    protected String getDataName(String path){
        String[] items = path.split("/");
        if(items == null){
            return null;
        }
        return items[items.length-1];
    }
}
