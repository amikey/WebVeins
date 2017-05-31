package com.xiongbeer.webveins;


import com.xiongbeer.webveins.service.protocol.message.ProcessDataProto;

/**
 * Created by shaoxiong on 17-4-9.
 */
public class Test {
    public static void main(String[] args) {
        ProcessDataProto.ProcessData.Builder builder = ProcessDataProto.ProcessData.newBuilder();
        builder.setType(1);
        System.out.println(builder.build());
    }
}
