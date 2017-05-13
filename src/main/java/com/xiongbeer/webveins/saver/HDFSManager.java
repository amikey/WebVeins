package com.xiongbeer.webveins.saver;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

/**
 * Created by shaoxiong on 17-4-13.
 */
public class HDFSManager {
    FileSystem fs;

    public HDFSManager(String hdfsFileSystem){
        Configuration conf = new Configuration();
        /*
         	打jar包时hadoop-common中的services会覆盖hadoop-hdfs中的services，
         	所以运行时会抛出java.io.IOException: No FileSystem for scheme: hdfs
         	设置这个属性便不会发生这个问题
         */
        conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        try {
            fs = FileSystem.get(URI.create(hdfsFileSystem), conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 只提供创建，不提供append
     * 主要是因为性能和稳定性的原因
     *
     * @param src
     * @param channel
     */
    public void createHDFSFile(String src, FileChannel channel) throws IOException {
        Path path = new Path(src);
        FSDataOutputStream out = fs.create(path, true);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while(channel.read(buffer) != -1){
            buffer.flip();
            while(buffer.hasRemaining()){
                out.write(buffer.get());
            }
            buffer.clear();
        }
    }

    /**
     * 删除HDFS中的文件
     *
     * @param path
     * @throws IOException
     */
    public void deleteHDFSFile(String path) throws IOException {
        fs.delete(new Path(path), false);
    }

    /**
     * 在HDFS中移动文件
     *
     * @param src
     * @param dst
     */
    public void moveHDFSFile(String src, String dst) throws IOException {
        fs.rename(new Path(src), new Path(dst));
    }

    /**
     * 判断文件是否存在
     *
     * @param path HDFS中的文件路径
     * @return
     */
    public boolean exist(String path) throws IOException {
        return fs.exists(new Path(path));
    }

    /**
     * 将本地文件上传到hdfs
     *
     * @param src 源文件路径
     * @param dst 目标文件路径
     * @throws IOException
     */
    public void upLoad(String src, String dst) throws IOException {
        fs.copyFromLocalFile(false, true, new Path(src), new Path(dst));
    }

    /**
     * 将hdfs上的文件下载到本地
     *
     * @param src 源文件路径
     * @param dst 目标文件路径
     * @throws IOException
     */
    public void downLoad(String src, String dst) throws IOException {
        fs.copyToLocalFile(false, new Path(src), new Path(dst));
    }

    /**
     * 列出目录下的文件
     *
     * 注意：只会列出文件不会列出目录
     *
     * @param src 目标文件夹路径
     * @param recursive 是否递归
     * @return
     */
    public LinkedList<String> listChildren(String src, boolean recursive) throws IOException {
        LinkedList<String> filePath = new LinkedList<String>();
        RemoteIterator<LocatedFileStatus> iterator = fs.listFiles(new Path(src), recursive);
        while(iterator.hasNext()){
            LocatedFileStatus child = (LocatedFileStatus) iterator.next();
            filePath.add(child.getPath().toString());
        }
        return filePath;
    }
}
