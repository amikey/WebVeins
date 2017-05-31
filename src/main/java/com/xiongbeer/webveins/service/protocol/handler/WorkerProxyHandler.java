package com.xiongbeer.webveins.service.protocol.handler;

import com.xiongbeer.webveins.Configuration;
import com.xiongbeer.webveins.ZnodeInfo;
import com.xiongbeer.webveins.service.protocol.message.MessageType;
import com.xiongbeer.webveins.service.protocol.message.ProcessDataProto.ProcessData;
import com.xiongbeer.webveins.zk.worker.Worker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by shaoxiong on 17-5-30.
 */
public class WorkerProxyHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = LoggerFactory.getLogger(WorkerProxyHandler.class);
    private static String currentTask;
    private static ExecutorService workerLoop = Executors.newSingleThreadExecutor();
    private static ProcessData.Builder builder;
    private ScheduledExecutorService heartBeat = Executors.newScheduledThreadPool(1);
    private Worker worker;

    public WorkerProxyHandler(Worker worker){
        this.worker = worker;
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
        ProcessData message = (ProcessData) msg;
        if(message.getType() == MessageType.CRAWLER_REQ.getValue()) {
            workerLoop.execute(new CrawlerTask(ctx, message));
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    private void process(ChannelHandlerContext ctx, ProcessData data){
        String taskPath = ZnodeInfo.NEW_TASK_PATH + data.getUrlFileName();
        ProcessData.CrawlerStatus status = data.getStatus();
        builder = ProcessData.newBuilder();
        switch (status){
            case NULL:
                currentTask = null;
                logger.warn("Give up the task: " + taskPath);
                /*
                    TODO 目前暂时将放弃的任务放入黑名单，后面会设置定时器将其移除
                 */
                worker.addToBlackList(new File(taskPath).getName());
                worker.discardTask(taskPath);
                break;
            case READY:
                if(currentTask != null){
                    builder.setStatus(ProcessData.CrawlerStatus.RUNNING);
                    builder.setUrlFilePath(Configuration.WAITING_TASKS_URLS +
                            "/" + currentTask);
                    builder.setUrlFileName(currentTask);
                    ctx.writeAndFlush(builder.build());
                    break;
                }
                takeNewTask(ctx);
                break;
            case FINNISHED:
                heartBeat.shutdownNow();
                currentTask = null;
                worker.finishTask(taskPath);
                takeNewTask(ctx);
                break;
            case WAITING:
                break;
            /*
                TODO 任务文件有问题，放弃任务并且移除这个任务
                case ERROR
             */
            default:
                break;
        }
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
        builder.setStatus(ProcessData.CrawlerStatus.RUNNING);
        builder.setUrlFilePath(Configuration.WAITING_TASKS_URLS + "/" + taskName);
        builder.setUrlFileName(taskName);
        ctx.writeAndFlush(builder.build());

        /* 拿到任务后会定时改变任务的mtime，防止被manager错误的重置 */
        heartBeat.scheduleAtFixedRate(new HeartBeat(taskName), 0, Configuration.CHECK_TIME, TimeUnit.SECONDS);
    }

    class HeartBeat implements Runnable {
        String taskName;

        public HeartBeat(String taskName){
            this.taskName = taskName;
        }

        @Override
        public void run() {
            worker.beat(taskName);
        }
    }

    class CrawlerTask implements Runnable {
        ChannelHandlerContext ctx;
        ProcessData data;

        public CrawlerTask(ChannelHandlerContext ctx, ProcessData data){
            this.ctx = ctx;
            this.data = data;
        }

        @Override
        public void run() {
            process(ctx, data);
        }
    }
}
