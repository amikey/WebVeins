package com.xiongbeer.webveins.service.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.marshalling.MarshallingDecoder;
import io.netty.handler.codec.marshalling.UnmarshallerProvider;

/**
 * Created by shaoxiong on 17-5-28.
 */
public class WvMarshallingDecoder extends MarshallingDecoder{

    public WvMarshallingDecoder(UnmarshallerProvider provider, int maxObjectSize) {
        super(provider, maxObjectSize);
    }

    public Object decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        return super.decode(ctx, buf);
    }
}
