package com.xiongbeer.webveins.api.info;

import com.xiongbeer.webveins.ZnodeInfo;
import com.xiongbeer.webveins.api.SimpleInfo;
import com.xiongbeer.webveins.api.jsondata.JData;
import com.xiongbeer.webveins.api.jsondata.TaskJson;
import com.xiongbeer.webveins.exception.VeinsException.OperationFailedException;
import com.xiongbeer.webveins.zk.task.Task;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by shaoxiong on 17-5-12.
 */
public class TaskInfo implements SimpleInfo {
    private List<JData> info;
    private ZooKeeper zk;

    public TaskInfo(ZooKeeper zk){
        this.zk = zk;
        info = new LinkedList<JData>();
    }

    public TaskInfo getCurrentTasks(){
        try {
            List<String> children
                    = zk.getChildren(ZnodeInfo.TASKS_PATH, false);
            byte[] data;
            for(String child:children){
                Stat stat = new Stat();
                data = zk.getData(ZnodeInfo.NEW_TASK_PATH + child,
                        false, stat);
                info.add(taskInfo(stat, child, data));
            }
            return this;
        } catch (KeeperException.ConnectionLossException e) {
            throw new OperationFailedException("[Error] Connection loss" +
                    ", you may have to wait for a while.");
        } catch (KeeperException.AuthFailedException e) {
            throw new OperationFailedException("[Error] Authentication failed.");
        } catch (KeeperException.NoAuthException e) {
            throw new OperationFailedException("[Error] Permission denied.");
        } catch (KeeperException e) {
            throw new OperationFailedException("[Error] Unknow reason.");
        } catch (InterruptedException e) {
            throw new OperationFailedException("[Error] Interrupted.");
        }
    }

    private TaskJson taskInfo(Stat taskStat, String name, byte[] data){
        TaskJson foo = new TaskJson();
        String status = new String(data);
        foo.setStatus(status);
        int ftimes = getFailedTimes(taskStat.getVersion(), status);
        foo.setFailedTimes(ftimes);
        foo.setCtime(taskStat.getCtime());
        foo.setMtime(taskStat.getMtime());
        foo.setName(name);
        return foo;
    }

    /**
     * 获取任务历史失败次数
     *
     * @param mTimes 修改的次数
     * @param currentStatus 当前的状态
     * @return 失败次数
     */
    private int getFailedTimes(int mTimes, String currentStatus){
        int ftimes = 0;
        if(mTimes > 0) {
            switch (currentStatus) {
                case Task.WAITING:
                    ftimes = mTimes/2;
                    break;
                case Task.RUNNING:
                    ftimes = (mTimes-1)/2;
                    break;
                case Task.FINISHED:
                    ftimes = (mTimes-2)/2;
                    break;
                default:
                    break;
            }
        }
        return ftimes;
    }

    @Override
    public List<JData> getInfo() {
        return info;
    }
}
