package com.xiongbeer.webveins;

import com.xiongbeer.webveins.saver.HDFSManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by shaoxiong on 17-4-9.
 */
public class Test {

    public static void main(String[] args) throws SocketException {
        /*
        ScheduledExecutorService service = Executors
                .newSingleThreadScheduledExecutor();
        Runnable runnable = new Runnable() {
            public void run() {
                // task to run goes here

                }
            }
        };
        service.scheduleAtFixedRate(runnable, 5, 5, TimeUnit.SECONDS);
        */
        /*
        try {
            Process process = Runtime.getRuntime().exec("jps");
            InputStreamReader iR = new InputStreamReader(process.getInputStream());
            BufferedReader input = new BufferedReader(iR);
            String line;
            while((line = input.readLine()) != null){
                if(line.matches(".*DataNode")){
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
}
