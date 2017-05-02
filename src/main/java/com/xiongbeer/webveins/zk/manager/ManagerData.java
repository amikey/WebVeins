package com.xiongbeer.webveins.zk.manager;

import com.google.protobuf.InvalidProtocolBufferException;
import com.xiongbeer.webveins.service.BalanceDataProto;

/**
 * Created by shaoxiong on 17-5-2.
 */
public class ManagerData implements Comparable<ManagerData> {
    private BalanceDataProto.BalanceData data;
    private String path;
    private String ip;


    public ManagerData(byte[] dataFlow) throws InvalidProtocolBufferException {
        this.path = path;
        data = BalanceDataProto.BalanceData.parseFrom(dataFlow);
    }

    public void setData(byte[] dataFlow) throws InvalidProtocolBufferException {
        data = BalanceDataProto.BalanceData.parseFrom(dataFlow);
    }

    public Integer getLoad(){
        return new Integer(data.getLoad());
    }

    @Override
    public int compareTo(ManagerData o) {
        return this.getLoad().compareTo(o.getLoad());
    }
}
