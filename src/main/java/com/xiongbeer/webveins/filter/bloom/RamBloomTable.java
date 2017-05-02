package com.xiongbeer.webveins.filter.bloom;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 存储在内存中的Bloom过滤器
 * Created by shaoxiong on 17-4-11.
 */
public class RamBloomTable extends BloomTable{
    private static final int DEFAULT_SIZE = 2 << 24;
    private static final int[] DEFAULT_SEEDS = new int[]{7, 11, 13, 31, 37, 61};

    public RamBloomTable(){
        super(new BloomBitSet(DEFAULT_SIZE), DEFAULT_SIZE, DEFAULT_SEEDS);
    }

    public RamBloomTable(long size, int[] seeds){
        super(new BloomBitSet(size), size, seeds);
    }

    /**
     * 持久化当前状态
     * @param path
     * @throws IOException
     */
    public void save(String path) throws IOException {
        File file = new File(path);
        FileOutputStream fos = new FileOutputStream(file);
        FileChannel channel = fos.getChannel();
        ByteBuffer outBuffer = ByteBuffer.allocate(1024);
        int limit = outBuffer.limit();
        long index = 0;
        while(index < super.size) {
            while (outBuffer.position() < limit) {
                outBuffer.put((byte) (super.get(index) ? 1 : 0));
                ++index;
            }
            outBuffer.flip();
            channel.write(outBuffer);
            outBuffer.clear();
        }
        channel.close();
        fos.close();
    }


    /**
     * 读取保存的状态
     * @param path
     * @throws IOException
     */
    public void load(String path) throws IOException {
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        FileChannel channel = fis.getChannel();
        ByteBuffer outBuffer = ByteBuffer.allocate(1024);
        long index = 0;
        while(channel.read(outBuffer) != -1){
            outBuffer.flip();
            while(outBuffer.hasRemaining()){
                super.bits.set(index, outBuffer.get()==0?false:true);
                ++index;
            }
            outBuffer.clear();
        }
        channel.close();
        fis.close();
    }
}
