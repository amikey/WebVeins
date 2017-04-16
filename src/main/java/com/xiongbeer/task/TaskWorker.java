package com.xiongbeer.task;

import com.xiongbeer.Tracker;
import com.xiongbeer.ZnodeInfo;
import org.apache.zookeeper.*;

import java.util.*;

/**
 * Created by shaoxiong on 17-4-10.
 */
public class TaskWorker extends Task{

    //TODO 构建优先级队列时候可以用到
    private LinkedList<String> waitingList = new LinkedList<String>();

    public TaskWorker(ZooKeeper zk) {
        super(zk);
    }

    /**
     * 接管任务
     */
    public boolean takeTask(){
        Tracker tracker = new Tracker();
        String task = null;
        checkTasks(tracker);
        while(tracker.getStatus() == Tracker.WAITING){
            /* 等待checkTasks任务完成 */
        }

        /* 抢夺未被领取的任务 */
        Iterator iterator = tasksInfo.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry entry = (Map.Entry)iterator.next();
            String key = (String) entry.getKey();
            Epoch value = (Epoch) entry.getValue();
            if(value.getStatus().equals(WAITING)){
                if(setRunningTask(ZnodeInfo.TASKS_PATH + "/" + key,
                        value.getDataVersion())) {
                    task = key;
                    break;
                }
            }
        }

        /* 如果task不为null就说明拿到了任务 */
        if(task != null){
            return true;
        }

        return false;
    }

    /**
     * 设置工作状态，方便manager维护
     *
     * @param status free为等待任务，非free
     *               状态应传入task的名称
     */
    public void setStatus(String status, String workerPath){
        try {
            client.setData(workerPath, status.getBytes(), -1);
        } catch (KeeperException.ConnectionLossException e) {
            setStatus(status, workerPath);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    /**
     * 尝试将一个task节点置为running状态
     * 若成功即拿到了该任务
     *
     * @param path
     * @param version
     * @return
     */
    private boolean setRunningTask(String path, int version){
        boolean result = false;
        try {
            client.setData(path, RUNNING.getBytes(), version);
            result = true;
        } catch (KeeperException.ConnectionLossException e) {
            setRunningTask(path, version);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
        return result;
    }
}
