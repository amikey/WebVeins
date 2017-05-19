package com.xiongbeer.webveins.service.local;

import com.xiongbeer.webveins.Configuration;
import com.xiongbeer.webveins.saver.HDFSManager;
import com.xiongbeer.webveins.service.ProcessDataProto;
import com.xiongbeer.webveins.utils.IdProvider;
import com.xiongbeer.webveins.utils.MD5Maker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Set;

/**
 * 提供给用户的使用接口，引导入口
 *
 * Created by shaoxiong on 17-4-26.
 */
public class Bootstrap {
    private Action action;
    private Client client;
    private static int WIRTE_LENGTH = 1024;
    private static HDFSManager hdfsManager;
    private static IdProvider idProvider;
    private static final String savePath;
    static{
    	Configuration.getInstance();

    	/* 持久化至本地的TEMP_DIR */
    	savePath = Configuration.TEMP_DIR;
    	idProvider = new IdProvider();
    	hdfsManager = new HDFSManager(Configuration.HDFS_SYSTEM_CONF
                , Configuration.HDFS_SYSTEM_PATH);
    }
    
    public Bootstrap(Action action){
        this.action = action;
    }
    
    public String getSavePath(){
    	return savePath;
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

    public Bootstrap ready()
            throws IOException {
        ProcessDataProto.ProcessData.Builder builder =
                ProcessDataProto.ProcessData.newBuilder();
        builder.setStatus(ProcessDataProto.ProcessData.Status.READY);
        builder.setUrlFilePath("");
        client.sentData(builder.build());
        return this;
    }

    /**
     * 将新的Urls持久化到本地然后上传至HDFS
     *
     * @param newUrls
     * @return 返回本地保存文件的路径
     */
    public static String upLoadNewUrls(Set<String> newUrls) throws IOException {
        /* 临时文件名：本机ip+生成时间 */
        String path = savePath + File.separator;
        String tempName = idProvider.getIp() +
                '@' + System.currentTimeMillis();
        MD5Maker md5Maker = new MD5Maker();
        File file = new File(path+tempName);
        FileOutputStream fos = new FileOutputStream(file);
        FileChannel channel = fos.getChannel();
        ByteBuffer outBuffer = ByteBuffer.allocate(WIRTE_LENGTH);
        for(String url:newUrls){
            String line = url+'\n';
            md5Maker.update(line);
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
        String newName = md5Maker.toString();
        file.renameTo(new File(path+newName));
        /* 上传至HDFS */
        hdfsManager.upLoad(path+newName, Configuration.NEW_TASKS_URLS);
        return path;
    }

    public Bootstrap stopClient(){
        client.disconnect();
        return this;
    }
}
