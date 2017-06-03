package com.xiongbeer.webveins.service.protocol.handler;

import com.xiongbeer.webveins.service.protocol.message.MessageType;
import com.xiongbeer.webveins.service.protocol.message.ProcessDataProto.ProcessData;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by shaoxiong on 17-5-30.
 */
public class ShellReqHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        ProcessData resp = (ProcessData) msg;
        if(resp.getType() == MessageType.SHELL_RESP.getValue()) {
            if (resp.getCommandReasult() == null) {
                ProcessData req = buildShellReq(resp.getCommand());
                ctx.writeAndFlush(req);
            } else {
                System.out.println(resp.getCommandReasult());
                ctx.close();
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
    }

    private ProcessData buildShellReq(String command){
        ProcessData.Builder builder = ProcessData.newBuilder();
        builder.setType(MessageType.SHELL_REQ.getValue());
        builder.setCommand(command);
        return builder.build();
    }

}
