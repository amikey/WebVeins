package com.xiongbeer.webveins.service.protocol;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.marshalling.MarshallerProvider;
import io.netty.handler.codec.marshalling.MarshallingEncoder;



/**
 * Created by shaoxiong on 17-5-28.
 */
public class WvMarshallingEncoder extends MarshallingEncoder {
    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

    public WvMarshallingEncoder(MarshallerProvider provider) {
        super(provider);
    }

    public void encode(ChannelHandlerContext ctx, Object msg, ByteBuf buf) throws Exception{
        super.encode(ctx, msg, buf);
    }
}
