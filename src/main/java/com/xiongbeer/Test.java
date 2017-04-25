package com.xiongbeer;

import com.xiongbeer.saver.HDFSManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by shaoxiong on 17-4-9.
 */
public class Test {

    public static void main(String[] args){
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
    }
}
