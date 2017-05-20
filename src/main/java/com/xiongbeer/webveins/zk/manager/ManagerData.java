package com.xiongbeer.webveins.zk.manager;

import com.google.protobuf.InvalidProtocolBufferException;
import com.xiongbeer.webveins.service.balance.BalanceDataProto;

/**
 * Created by shaoxiong on 17-5-2.
 */
public class ManagerData implements Comparable<ManagerData> {
    private BalanceDataProto.BalanceData data;

    public ManagerData(byte[] dataFlow) throws InvalidProtocolBufferException {
        data = BalanceDataProto.BalanceData.parseFrom(dataFlow);
    }

    public void setData(byte[] dataFlow) throws InvalidProtocolBufferException {
        data = BalanceDataProto.BalanceData.parseFrom(dataFlow);
    }

    public Integer getLoad(){
        return new Integer(data.getLoad());
    }

    public String getIp(){
        return data.getIp();
    }

    public int getPort(){
        return data.getPort();
    }

    public String getZKConnectString(){
        return data.getZkIp() + ":" + data.getZkPort();
    }

    @Override
    public int compareTo(ManagerData o) {
        return this.getLoad().compareTo(o.getLoad());
    }
}
