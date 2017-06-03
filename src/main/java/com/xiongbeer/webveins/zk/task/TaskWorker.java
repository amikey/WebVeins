package com.xiongbeer.webveins.zk.task;

import com.xiongbeer.webveins.ZnodeInfo;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;

import java.util.*;
import java.util.Map.Entry;

/**
 * Created by shaoxiong on 17-4-10.
 */
public class TaskWorker extends Task{

    /* 任务黑名单 */
    private static List<String> blackList = new LinkedList<String>();
    public TaskWorker(CuratorFramework client) {
        super(client);
    }

    /**
     * 接管任务
     *
     * 这里可能会有一个疑问，那就是为何不使用sync
     * 就目前而言，还找不到使用它的理由，因为强实时性的意义并不大，
     * 即使本地zookeeper的视图稍有落后，也并不会发生多个worker持有一个任务的情况发生（会验证Task的版本信息）
     * 只是会多一些抢夺次数，而频繁的sync可能会给服务器带来更大的负担
     */
    public String takeTask(){
        String task = null;
        checkTasks();
        /* 抢夺未被领取的任务 */
        Iterator<Entry<String, Epoch>> iterator = super.tasksInfo.entrySet().iterator();
        while(iterator.hasNext()){
            @SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry)iterator.next();
            String key = (String) entry.getKey();
            Epoch value = (Epoch) entry.getValue();
            if(!blackList.contains(value) && value.getStatus() == Status.WAITING){
                if(setRunningTask(ZnodeInfo.TASKS_PATH + "/" + key,
                        value.getDataVersion())) {
                    task = key;
                    break;
                }
            }
        }

        /* 如果task不为null就说明拿到了任务 */
        return task;
    }

    public static void clearTaskBlackList(){
        blackList.clear();
    }

    public static void removeTaskBlackListElement(String taskName){
        blackList.remove(taskName);
    }

    public static void addToBlackList(String taskName){
        blackList.add(taskName);
    }

    /**
     * 执行失败，放弃任务
     *
     * @param taskPath
     *
     */
    public void discardTask(String taskPath){
        try {
            client.setData().forPath(taskPath, Status.WAITING.getValue().getBytes());
        } catch (KeeperException.ConnectionLossException e){
            discardTask(taskPath);
        } catch (Exception e) {
            logger.warn("discard task" + taskPath + " failed", e);
        }
    }

    /**
     * 完成任务
     *
     * @param taskPath
     */
    public void finishTask(String taskPath){
        try {
            client.setData().forPath(taskPath, Status.FINISHED.getValue().getBytes());
        } catch (KeeperException.ConnectionLossException e) {
            finishTask(taskPath);
        } catch (Exception e) {
            logger.error("set task" + taskPath + " finished failed", e);
        }
    }


    /**
     * 尝试将一个task节点置为running状态
     * 若成功即拿到了该任务
     * 也可作为心跳信息（改变了mtime）
     *
     * @param path
     * @param version
     * @return
     */
    public boolean setRunningTask(String path, int version){
        boolean result = false;
        try {
            client.setData().withVersion(version).forPath(path, Status.RUNNING.getValue().getBytes());
            result = true;
        } catch (KeeperException.NoNodeException e) {
            super.tasksInfo.remove(path);
        } catch (KeeperException.ConnectionLossException e) {
            setRunningTask(path, version);
        } catch (Exception e) {
            logger.warn("set running task failed.", e);
        }
        return result;
    }
}
