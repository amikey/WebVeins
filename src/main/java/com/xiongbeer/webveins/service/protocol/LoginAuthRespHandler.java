package com.xiongbeer.webveins.service.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.xiongbeer.webveins.service.protocol.Message.Coderc;

/**
 * Created by shaoxiong on 17-5-28.
 */
public class LoginAuthRespHandler extends ChannelInboundHandlerAdapter {
    private Map<String, Boolean> nodeCheck = new ConcurrentHashMap<String, Boolean>();
    private String[] whitekList = {"127.0.0.1", "192.168.1.104"};

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        Message message = (Message)msg;
        if (message.getHeader() != null && message.getHeader().getType() == Coderc.LOGIN_REQ.getValue()) {
            String nodeIndex = ctx.channel().remoteAddress().toString();
            Message loginResult = null;
            if (nodeCheck.containsKey(nodeIndex)) {
                loginResult = buildRespon(Coderc.DUPLICATE_LOGIN.getValue());
            } else {
                InetSocketAddress address = (InetSocketAddress)ctx.channel().remoteAddress();
                String ip = address.getAddress().getHostAddress();
                boolean isOK = false;
                for (String WIP : whitekList) {
                    if (WIP.equals(ip)) {
                        isOK = true;
                        break;
                    }
                }
                loginResult = isOK ? buildRespon(Coderc.LOGIN_ACCEPTED.getValue())
                        : buildRespon(Coderc.LOGIN_REFUSED.getValue());
                if (isOK) {
                    nodeCheck.put(nodeIndex, true);
                }
            }
            System.out.println("the login response is : " + loginResult);
            ctx.writeAndFlush(loginResult);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private Message buildRespon(byte result) {
        Message message = new Message();
        Header header = new Header();
        header.setType(Coderc.LOGIN_RESP.getValue());
        message.setHeader(header);
        message.setBody(result);
        return message;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        nodeCheck.remove(ctx.channel().remoteAddress().toString()); //出现异常删除缓存
        ctx.close();
        ctx.fireExceptionCaught(cause);
    }
}
