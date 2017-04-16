package com.xiongbeer;

import com.xiongbeer.saver.HDFSManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by shaoxiong on 17-4-9.
 */
public class Test {

    public static void main(String[] args){
        HDFSManager hdfsManager = new HDFSManager("hdfs://localhost:9000/");
        File file = new File(Configuration.TEMP_DIR);
        File[] files = file.listFiles();
        for(File f:files){
            String path = f.getAbsolutePath();
            try {
                hdfsManager.upLoad(path, Configuration.WAITING_TASKS_URLS );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
