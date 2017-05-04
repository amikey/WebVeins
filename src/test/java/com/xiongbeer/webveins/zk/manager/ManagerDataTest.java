package com.xiongbeer.webveins.zk.manager;

import com.google.protobuf.InvalidProtocolBufferException;
import com.xiongbeer.webveins.service.BalanceDataProto;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by shaoxiong on 17-5-4.
 */
public class ManagerDataTest {
    BalanceDataProto.BalanceData.Builder builder =
            BalanceDataProto.BalanceData.newBuilder();

    @Test
    public void sortTest() throws InvalidProtocolBufferException {
        builder.setZkPort(0);
        builder.setZkIp("");
        builder.setIp("");
        builder.setPort(0);
        List<ManagerData> data = new LinkedList<ManagerData>();
        builder.setLoad(3);
        data.add(new ManagerData(builder.build().toByteArray()));
        builder.setLoad(4);
        data.add(new ManagerData(builder.build().toByteArray()));
        builder.setLoad(1);
        data.add(new ManagerData(builder.build().toByteArray()));
        builder.setLoad(0);
        data.add(new ManagerData(builder.build().toByteArray()));
        Collections.sort(data);
        assertEquals(data.get(0).getLoad(), new Integer(0));
    }
}
