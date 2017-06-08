package com.xiongbeer.webveins.service.protocol.handler;

import com.google.common.primitives.UnsignedInteger;
import com.google.protobuf.ByteString;
import com.xiongbeer.webveins.Configuration;
import com.xiongbeer.webveins.ZnodeInfo;
import com.xiongbeer.webveins.service.protocol.message.MessageType;
import com.xiongbeer.webveins.service.protocol.message.ProcessDataProto.ProcessData;
import com.xiongbeer.webveins.zk.task.Epoch;
import com.xiongbeer.webveins.zk.task.Task;
import com.xiongbeer.webveins.zk.task.TaskData;
import com.xiongbeer.webveins.zk.worker.Worker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.concurrent.*;

/**
 * Created by shaoxiong on 17-5-30.
 */
public class WorkerProxyHandler extends ChannelInboundHandlerAdapter {
    public static ExecutorService workerLoop = Executors.newSingleThreadExecutor();
    private static Logger logger = LoggerFactory.getLogger(WorkerProxyHandler.class);
    private volatile ScheduledFuture<?> heartBeat;
    private volatile int progress;
    private static Epoch currentTask;
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
        MessageType type = MessageType.get((byte) message.getType());
        if(type == MessageType.CRAWLER_REQ) {
            workerLoop.execute(new CrawlerTask(ctx, message));
        } else if(type == MessageType.HEART_BEAT_REQ){
            try {
                progress = Integer.parseInt(message.getAttachment().toString(Charset.defaultCharset()));
            } catch (NumberFormatException e){
                // pass
                logger.error("server workerproxy progress");
            }
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
        ProcessData.Builder builder = ProcessData.newBuilder();
        switch (status){
            case NULL:
                currentTask = null;
                logger.warn("Give up the task: " + taskPath);
                /*
                    TODO 目前暂时将放弃的任务放入黑名单，后面会设置定时器将其移除
                 */
                Worker.addToBlackList(new File(taskPath).getName());
                worker.discardTask(taskPath);
                break;
            case READY:
                if(currentTask != null){
                    builder.setType(MessageType.CRAWLER_RESP.getValue());
                    builder.setStatus(ProcessData.CrawlerStatus.RUNNING);
                    builder.setUrlFilePath(Configuration.WAITING_TASKS_URLS +
                            "/" + currentTask);
                    builder.setUrlFileName(currentTask.getTaskName());
                    ctx.writeAndFlush(builder.build());
                    break;
                }
                takeNewTask(ctx);
                break;
            case FINNISHED:
                currentTask = null;
                heartBeat.cancel(true);
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
        Epoch task;
        while(true) {
            logger.info("Waiting for task...");
            worker.waitForTask();
            logger.info("Trying to get a task...");
            task = worker.takeTask();
            if(task != null) {
                logger.info("Get task: " + task + " crawler start working...");
                currentTask = task;
                break;
            }
        }
        ProcessData.Builder builder = ProcessData.newBuilder();
        builder.setType(MessageType.CRAWLER_RESP.getValue());
        builder.setStatus(ProcessData.CrawlerStatus.RUNNING);
        builder.setUrlFilePath(Configuration.WAITING_TASKS_URLS + "/" + task.getTaskName());
        builder.setUrlFileName(task.getTaskName());
        ctx.writeAndFlush(builder.build());

        /* 拿到任务后会定时改变任务的mtime，防止被manager错误的重置 */
        heartBeat = ctx
                .channel()
                .eventLoop()
                .scheduleAtFixedRate(new HeartBeat(task.getTaskName()
                        , task.getTaskData().getUniqueMarkup(), ctx.channel()), 0
                        , Configuration.WORKER_HEART_BEAT, TimeUnit.SECONDS);
    }

    class HeartBeat implements Runnable {
        String taskName;
        TaskData taskData;
        Channel channel;

        public HeartBeat(String taskName, UnsignedInteger markup, Channel channel){
            taskData = new TaskData();
            taskData.setStatus(Task.Status.RUNNING).setUniqueMarkup(markup.intValue());
            this.taskName = taskName;
            this.channel = channel;
        }

        @Override
        public void run() {
            if(channel == null){
                logger.warn("crawler client lose connection");
                heartBeat.cancel(true);
            }
            TaskData taskData = new TaskData();
            taskData.setProgress(progress);
            worker.beat(taskName, taskData);
            ProcessData.Builder builder = ProcessData.newBuilder();
            builder.setType(MessageType.HEART_BEAT_RESP.getValue());
            builder.setStatus(ProcessData.CrawlerStatus.RUNNING);
            builder.setAttachment(ByteString.copyFrom(new Integer(progress).toString().getBytes()));
            channel.writeAndFlush(builder.build());
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
