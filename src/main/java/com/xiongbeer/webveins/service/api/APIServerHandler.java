package com.xiongbeer.webveins.service.api;


import com.xiongbeer.webveins.Configuration;
import com.xiongbeer.webveins.api.Command;
import com.xiongbeer.webveins.api.OutputFormatter;
import com.xiongbeer.webveins.api.info.FilterInfo;
import com.xiongbeer.webveins.api.info.TaskInfo;
import com.xiongbeer.webveins.api.info.WorkerInfo;
import com.xiongbeer.webveins.api.jsondata.JData;
import com.xiongbeer.webveins.saver.HDFSManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by shaoxiong on 17-5-13.
 */
public class APIServerHandler extends ChannelInboundHandlerAdapter {
    private ZooKeeper zk;
    private HDFSManager hdfsManager;

    public APIServerHandler(ZooKeeper zk, HDFSManager hdfsManager){
        this.zk = zk;
        this.hdfsManager = hdfsManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Command result = analysis((String)msg);
        byte[] content = (operation(result)
                + System.getProperty("line.separator"))
                .getBytes();
        ByteBuf message = Unpooled.buffer(content.length);
        message.writeBytes(content);
        ctx.writeAndFlush(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    private Command analysis(String req){
        EnumSet<Command> commands = EnumSet.allOf(Command.class);
        for(Command command:commands){
            Pattern pattern = Pattern.compile(command.toString());
            Matcher matcher = pattern.matcher(req.toUpperCase());
            while(matcher.find()){
                return command;
            }
        }
        return null;
    }

    private String operation(Command command){
        List<JData> dataSet = null;
        String result = null;
        if(command == null){
            return "[Error] Empty input";
        }
        switch (command){
            case LISTTASKS:
                TaskInfo taskInfo  = new TaskInfo(zk);
                dataSet = taskInfo.getCurrentTasks().getInfo();
                result = JDecoder(dataSet);
                break;
            case LISTFILTERS:
                FilterInfo filterInfo = new FilterInfo(zk, hdfsManager);
                try {
                    dataSet = filterInfo
                            .getBloomCacheInfo(Configuration.BLOOM_BACKUP_PATH)
                            .getInfo();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                result = JDecoder(dataSet);
                break;
            case LISTWORKERS:
                WorkerInfo workerInfo = new WorkerInfo(zk);
                dataSet = workerInfo.getCurrentWoker().getInfo();
                result = JDecoder(dataSet);
                break;
            default:
                break;
        }
        return result;
    }

    private String JDecoder(List<JData> dataSet){
        if(dataSet == null|| dataSet.size() == 0){
            return "Unknow Error";
        }
        return new OutputFormatter(dataSet).format();
    }
}
