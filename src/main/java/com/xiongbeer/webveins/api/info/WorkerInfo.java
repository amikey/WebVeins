package com.xiongbeer.webveins.api.info;

import com.xiongbeer.webveins.ZnodeInfo;
import com.xiongbeer.webveins.api.SimpleInfo;
import com.xiongbeer.webveins.api.jsondata.JData;
import com.xiongbeer.webveins.api.jsondata.WorkerJson;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.LinkedList;
import java.util.List;


/**
 * Created by shaoxiong on 17-5-12.
 */
public class WorkerInfo implements SimpleInfo {
    private List<JData> info;
    private ZooKeeper zk;

    public WorkerInfo(ZooKeeper zk){
        this.info = new LinkedList<JData>();
        this.zk = zk;
    }

    public WorkerInfo getCurrentWoker(){
        List<String> children;
        try {
            children = zk.getChildren(ZnodeInfo.WORKERS_PATH, false);
            for(String child:children){
                WorkerJson data = new WorkerJson();
                Stat stat = new Stat();
                byte[] content;
                content = zk.getData(ZnodeInfo.WORKERS_PATH + '/' + child
                        , false, stat);
                data.setName(child);
                /* 32为md5码长度 */
                if(content.length == 32){
                    data.setCurrentTask(new String(content));
                }
                else{
                    data.setCurrentTask("Empty");
                }
                info.add(data);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public List<JData> getInfo() {
        return info;
    }
}
