package com.xiongbeer.service;

import com.xiongbeer.ZnodeInfo;
import com.xiongbeer.zk.task.TaskWorker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by shaoxiong on 17-4-23.
 */
public class ServerHandler extends ChannelInboundHandlerAdapter{
    private Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private TaskWorker taskWorker;
    public ServerHandler(TaskWorker taskWorker){
        this.taskWorker = taskWorker;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Server.getChannels().add(ctx.channel());
        logger.info(ctx.channel().remoteAddress().toString() + " log in "
                + "at {}", new Date().toString());
    }

    /**
     * 爬虫Client返回给Server的信息
     * 任务成功则将任务状态设置为Finished
     * 让Manager自动回收
     * 失败则设置为Waiting，将任务让给其他
     * Worker
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ProcessDataProto.ProcessData data = (ProcessDataProto.ProcessData) msg;
        String taskPath = ZnodeInfo.NEW_TASK_PATH + data.getUrlFilePath();
        ProcessDataProto.ProcessData.Status status = data.getStatus();
        if(status == ProcessDataProto.ProcessData.Status.FINNISHED){
            taskWorker.FinishTask(taskPath);
        }else if(status == ProcessDataProto.ProcessData.Status.NULL){
            taskWorker.DiscardTask(taskPath);
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Server.getChannels().remove(ctx.channel());
        logger.info(ctx.channel().remoteAddress().toString() + " log out "
                + "at {}", new Date().toString());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
