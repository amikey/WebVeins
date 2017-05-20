package com.xiongbeer.webveins.service.local;

import com.xiongbeer.webveins.Configuration;
import com.xiongbeer.webveins.ZnodeInfo;
import com.xiongbeer.webveins.service.ProcessDataProto;
import com.xiongbeer.webveins.zk.worker.Worker;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by shaoxiong on 17-4-23.
 */
@ChannelHandler.Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter{
    private Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private static String currentTask;
    private Timer heartBeat;
    private Worker worker;

    /* 新任务的Data info的builder */
    private static ProcessDataProto.ProcessData.Builder builder;

    public ServerHandler(Worker worker){
        this.worker = worker;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Server.getChannels().add(ctx.channel());
        logger.info(ctx.channel().remoteAddress().toString() + " log in "
                + "at {}", new Date().toString());
    }

    /**
     * 爬虫Client返回给Server的信息
     * 任务成功则将任务状态设置为Finished，让Manager自动回收
     * 失败则设置为Waiting，将任务让给其他Worker
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ProcessDataProto.ProcessData data = (ProcessDataProto.ProcessData) msg;
        String taskPath = ZnodeInfo.NEW_TASK_PATH + data.getUrlFileName();
        ProcessDataProto.ProcessData.Status status = data.getStatus();
        builder = ProcessDataProto.ProcessData.newBuilder();
        switch (status){
            case NULL:
                currentTask = null;
                logger.warn("Give up the task: " + taskPath);
                worker.discardTask(taskPath);
                break;
            case READY:
                if(currentTask != null){
                    builder.setStatus(ProcessDataProto.ProcessData.Status.RUNNING);
                    builder.setUrlFilePath(Configuration.WAITING_TASKS_URLS +
                            "/" + currentTask);
                    builder.setUrlFileName(currentTask);
                    ctx.writeAndFlush(builder.build());
                    break;
                }
                takeNewTask(ctx);
                break;
            case FINNISHED:
                heartBeat.purge();
                heartBeat.cancel();
                currentTask = null;
                worker.finishTask(taskPath);
                takeNewTask(ctx);
                break;
            case WAITING:
                break;
            default:
                break;
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

    private void takeNewTask(ChannelHandlerContext ctx){
        String taskName;
        while(true) {
            logger.info("Waiting for task...");
            worker.waitForTask();
            logger.info("Trying to get a task...");
            taskName = worker.takeTask();
            if(taskName != null) {
                logger.info("Get task: " + taskName + " crawler start working...");
                currentTask = taskName;
                break;
            }
        }
        builder.setStatus(ProcessDataProto.ProcessData.Status.RUNNING);
        builder.setUrlFilePath(Configuration.WAITING_TASKS_URLS + "/" + taskName);
        builder.setUrlFileName(taskName);
        ctx.writeAndFlush(builder.build());

        /* 拿到任务后会定时改变任务的mtime，防止被manager错误的重置 */
        TimerTask heart = new HeartBeat(taskName);
        heartBeat = new Timer();
        long delay = Configuration.WORKER_HEART_BEAT;
        long intevalPeriod = Configuration.WORKER_HEART_BEAT * 1000;
        heartBeat.scheduleAtFixedRate(heart, delay, intevalPeriod);
    }

    class HeartBeat extends TimerTask{
        String taskName;
        public HeartBeat(String taskName){
            this.taskName = taskName;
        }

        @Override
        public void run() {
            worker.beat(taskName);
        }
    }
}
