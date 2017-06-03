package com.xiongbeer.webveins.api.info;

import com.xiongbeer.webveins.ZnodeInfo;
import com.xiongbeer.webveins.api.SimpleInfo;
import com.xiongbeer.webveins.api.jsondata.JData;
import com.xiongbeer.webveins.api.jsondata.TaskJson;
import com.xiongbeer.webveins.exception.VeinsException.OperationFailedException;
import com.xiongbeer.webveins.zk.task.Task;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by shaoxiong on 17-5-12.
 */
public class TaskInfo implements SimpleInfo {
    private List<JData> info;
    private CuratorFramework client;

    public TaskInfo(CuratorFramework client){
        this.client = client;
        info = new LinkedList<JData>();
    }

    public TaskInfo getCurrentTasks(){
        try {
            List<String> children =
                    client.getChildren().forPath(ZnodeInfo.TASKS_PATH);
            byte[] data;
            for(String child:children){
                Stat stat = new Stat();
                data = client.getData()
                        .storingStatIn(stat)
                        .forPath(ZnodeInfo.NEW_TASK_PATH + child);
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
        } catch (Exception e) {
            throw new OperationFailedException("[Error] Unknow reason." + e.getMessage());
        }
    }

    private TaskJson taskInfo(Stat taskStat, String name, byte[] data){
        TaskJson foo = new TaskJson();
        Task.Status status = Task.Status.get(new String(data));
        foo.setStatus(status);
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
    @SuppressWarnings("unused")
	@Deprecated
    private int getFailedTimes(int mTimes, Task.Status currentStatus){
        int ftimes = 0;
        if(mTimes > 0) {
            switch (currentStatus) {
                case WAITING:
                    ftimes = mTimes/2;
                    break;
                case RUNNING:
                    ftimes = (mTimes-1)/2;
                    break;
                case FINISHED:
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
