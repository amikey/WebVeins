package com.xiongbeer.webveins.service;

import com.xiongbeer.webveins.Configuration;
import com.xiongbeer.webveins.saver.HDFSManager;
import com.xiongbeer.webveins.utils.IdProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * 提供给用户的使用接口
 *
 * Created by shaoxiong on 17-4-26.
 */
public class Bootstrap {
    private Action action;
    private Client client;
    private static int WIRTE_LENGTH = 1024;
    private HDFSManager hdfsManager;
    private IdProvider idProvider;
    public Bootstrap(Action action){
        Configuration.getInstance();
        this.hdfsManager = new HDFSManager(Configuration.HDFS_SYSTEM_PATH);
        this.idProvider = new IdProvider();
        this.action = action;

    }

    public Bootstrap setAction(Action action){
        this.action = action;
        return this;
    }

    public Bootstrap runClient(){
        if(client == null) {
            client = new Client();
        }
        client.setAction(action);
        new Thread("wvLocalClient") {
            @Override
            public void run() {
                client.connect(Configuration.LOCAL_HOST, Configuration.LOCAL_PORT);
            }
        }.start();
        return this;
    }

    public Bootstrap ready(ProcessDataProto.ProcessData data, List<String> newUrls)
            throws IOException {
        if(newUrls != null){
            upLoadNewUrls(newUrls);
        }
        client.sentData(data);
        return this;
    }

    /**
     * 将新的Urls持久化到本地然后上传至HDFS
     *
     * @param newUrls
     */
    public void upLoadNewUrls(List<String> newUrls) throws IOException {
        /* 持久化至本地的TEMP_DIR */
        String root = Configuration.TEMP_DIR;
        /* 文件名：本机ip+生成时间 */
        /* TODO 在考虑要不要换为对文件生成MD5来命名 */
        String path = root + File.separator +idProvider.getIp() +
                '@' + System.currentTimeMillis();
        File file = new File(path);
        FileOutputStream fos = new FileOutputStream(file);
        FileChannel channel = fos.getChannel();
        ByteBuffer outBuffer = ByteBuffer.allocate(WIRTE_LENGTH);
        for(String url:newUrls){
            String line = url+'\n';
            byte[] data = line.getBytes();
            int len = data.length;
            for(int i=0; i<=len/WIRTE_LENGTH; ++i){
                outBuffer.put(data, i*WIRTE_LENGTH,
                        i==len/WIRTE_LENGTH?len%WIRTE_LENGTH:WIRTE_LENGTH);
                outBuffer.flip();
                channel.write(outBuffer);
                outBuffer.clear();
            }
        }
        channel.close();
        fos.close();

        /* 上传至HDFS */
        hdfsManager.upLoad(path, Configuration.NEW_TASKS_URLS);
    }

    public Bootstrap stopClient(){
        client.disconnect();
        return this;
    }
}
