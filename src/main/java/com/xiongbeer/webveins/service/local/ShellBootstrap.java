package com.xiongbeer.webveins.service.local;

import com.xiongbeer.webveins.Configuration;
import com.xiongbeer.webveins.service.protocol.Client;
import com.xiongbeer.webveins.service.protocol.message.MessageType;
import com.xiongbeer.webveins.service.protocol.message.ProcessDataProto.ProcessData;
import com.xiongbeer.webveins.utils.InitLogger;

/**
 * Created by shaoxiong on 17-6-2.
 */
public class ShellBootstrap extends Bootstrap {

    public ShellBootstrap(String command){
        ProcessData.Builder builder = ProcessData.newBuilder();
        builder.setType(MessageType.SHELL_REQ.getValue());
        builder.setCommand(command);
        super.client = new Client(builder.build());
    }

    @Override
    public void ready() {
        init();
    }

    public static void main(String[] args){
        InitLogger.initEmpty();
        Configuration.getInstance();
        StringBuilder command = new StringBuilder();
        for(String arg:args){
            command.append(arg);
            command.append(" ");
        }
        System.out.println("[info] command: " + command.toString());
        //Bootstrap bootstrap = new ShellBootstrap(command.toString());
        Bootstrap bootstrap = new ShellBootstrap("listtasks");
        bootstrap.ready();
    }
}
