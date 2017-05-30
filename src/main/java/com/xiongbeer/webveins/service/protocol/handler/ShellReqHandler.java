package com.xiongbeer.webveins.service.protocol.handler;

import com.xiongbeer.webveins.service.protocol.message.Header;
import com.xiongbeer.webveins.service.protocol.message.Message;
import com.xiongbeer.webveins.service.protocol.message.Message.Coderc;
import com.xiongbeer.webveins.service.ProcessDataProto.ProcessData;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by shaoxiong on 17-5-30.
 */
public class ShellReqHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        Header header = ((Message)msg).getHeader();
        ProcessData resp = ((Message)msg).getBody();
        if(header.getType() == Coderc.SHELL_RESP.getValue()) {
            if (resp.getCommandReasult() == null) {
                Message message = buildShellReq(resp.getCommand());
                ctx.writeAndFlush(message);
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

    private Message buildShellReq(String command){
        Message message = new Message();
        Header header = new Header();
        header.setType(Message.Coderc.SHELL_REQ.getValue());
        message.setBody(buildResult(command));
        message.setHeader(header);
        return message;
    }

    private ProcessData buildResult(String command){
        ProcessData.Builder builder = ProcessData.newBuilder();
        return builder.setCommand(command).build();
    }
}
