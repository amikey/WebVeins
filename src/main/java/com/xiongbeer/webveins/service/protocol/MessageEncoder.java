package com.xiongbeer.webveins.service.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;
import java.util.Map;

/**
 * Created by shaoxiong on 17-5-28.
 */
public class MessageEncoder extends MessageToMessageEncoder<Message>{
    WvMarshallingEncoder marshallingEncoder;

    public MessageEncoder(){
        this.marshallingEncoder = MarshallingCodecFactory.buildMarshallingEncoder();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> list) throws Exception {
        System.out.println("hei!");
        Header header = msg.getHeader();
        if(msg == null || header == null)
            throw new Exception("The encode message is null");
        ByteBuf sendBuf = Unpooled.buffer();
        sendBuf.writeInt(header.getCrcCode());
        sendBuf.writeInt(header.getLength());
        sendBuf.writeLong(header.getSessionID());
        sendBuf.writeByte(header.getType());
        sendBuf.writeByte(header.getPriority());
        sendBuf.writeInt(header.getAttachment().size());
        String key = null;
        byte[] keyArray = null;
        Object value = null;
        for(Map.Entry<String, Object> param:header.getAttachment().entrySet()){
            key = param.getKey();
            keyArray = key.getBytes("UTF-8");
            sendBuf.writeInt(keyArray.length);
            sendBuf.writeBytes(keyArray);
            value = param.getValue();
            marshallingEncoder.encode(ctx, value, sendBuf);
        }
        key = null;
        keyArray = null;
        value = null;

        Object body = msg.getBody();
        if(body != null){
            marshallingEncoder.encode(ctx, body, sendBuf);
        }
        sendBuf.setInt(4, sendBuf.readableBytes());
        list.add(sendBuf);
    }
}
