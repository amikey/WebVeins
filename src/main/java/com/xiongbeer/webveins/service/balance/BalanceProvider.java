package com.xiongbeer.webveins.service.balance;

import com.google.protobuf.InvalidProtocolBufferException;
import com.xiongbeer.webveins.ZnodeInfo;
import com.xiongbeer.webveins.zk.manager.ManagerData;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shaoxiong on 17-5-1.
 */
public class BalanceProvider {
    private static BalanceProvider balanceProvider;
    private ZooKeeper zk;

    private BalanceProvider(){

    }

    /**
     * 使用前必须设置ZooKeeper客户端
     * @param zk
     */
    public void setZK(ZooKeeper zk){
        this.zk = zk;
    }

    public synchronized static BalanceProvider getInstance(){
        if(balanceProvider == null){
            balanceProvider = new BalanceProvider();
        }
        return balanceProvider;
    }

    public List<ManagerData> getBalanceItems(){
        List<ManagerData> items = new ArrayList<ManagerData>();
        try {
            List<String> children = zk.getChildren(ZnodeInfo.MANAGERS_PATH, false);
            for(String child:children){
                ManagerData data = new ManagerData(zk.getData(child, false, null));
                items.add(data);
            }
        } catch (KeeperException.ConnectionLossException e) {
            items = getBalanceItems();
        } catch(KeeperException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return items;
    }


}
