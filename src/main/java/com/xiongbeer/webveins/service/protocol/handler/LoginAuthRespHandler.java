package com.xiongbeer.webveins.service.protocol.handler;

import com.xiongbeer.webveins.service.protocol.message.Header;
import com.xiongbeer.webveins.service.protocol.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.xiongbeer.webveins.service.protocol.message.Message.Coderc;
import com.xiongbeer.webveins.service.ProcessDataProto.ProcessData;

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
        Coderc rc = Coderc.get(message.getHeader().getType());
        if (message.getHeader() != null && rc == Coderc.CRAWLER_REQ) {
            String nodeIndex = ctx.channel().remoteAddress().toString();
            Message loginResult = null;
            if (nodeCheck.containsKey(nodeIndex)) {
                loginResult = buildResp(Coderc.DUPLICATE_LOGIN.getValue());
                ctx.close();
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
                loginResult = isOK ? buildResp((byte) message.getBody().getType())
                        : buildResp(Coderc.LOGIN_REFUSED.getValue());
                if (isOK) {
                    nodeCheck.put(nodeIndex, true);
                } else{
                    ctx.close();
                }
            }
            System.out.println("the login response is : " + loginResult);
            ctx.writeAndFlush(loginResult);
        } else {
            switch (rc){
                case CRAWLER_REQ:
                    break;
                case SHELL_REQ:
                    break;
                case HEART_BEAT_REQ:
                    break;
                default:
                    System.out.println("Closed.");
                    ctx.close();
                    return;
            }
            ctx.fireChannelRead(msg);
        }
    }

    private Message buildResp(byte result) {
        Message message = new Message();
        Header header = new Header();
        header.setType(Coderc.LOGIN_RESP.getValue());
        message.setHeader(header);
        message.setBody(buildResult(result));
        return message;
    }

    private ProcessData buildResult(byte result){
        ProcessData.Builder builder = ProcessData.newBuilder();
        return builder.setType(result).build();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        nodeCheck.remove(ctx.channel().remoteAddress().toString()); //出现异常删除缓存
        ctx.close();
        ctx.fireExceptionCaught(cause);
    }
}
