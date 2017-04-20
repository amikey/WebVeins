package com.xiongbeer.filter.bloom;

import com.xiongbeer.Configuration;

import java.io.*;

/**
 * 持久化的BitSet
 *
 * Created by shaoxiong on 17-4-11.
 */
public class DiskBloomBitSet extends AbstractBitSet{
    ByteChecker checker;
    static final int BLOOM_WRITE_BLOCK = 1024*1024;

    public DiskBloomBitSet(long size, String savePath){
        File file = new File(savePath);
        if(!file.exists()){
            try {
                FileOutputStream out = new FileOutputStream(file);
                byte[] content = new byte[BLOOM_WRITE_BLOCK];
                initBytes(content);
                for(long i=0; i<(size/(long)BLOOM_WRITE_BLOCK + 1); ++i){
                    out.write(content);
                }
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        checker = new ByteChecker(file);
    }

    @Override
    public void set(long bitIndex, boolean value){
        checker.setByte(bitIndex, value?(byte)1:(byte)0);
    }

    @Override
    public void set(long bitIndex, byte value){
        checker.setByte(bitIndex, value);
    }

    @Override
    public boolean get(long bitIndex){
        byte value = checker.readByte(bitIndex);
        return value == 0?false:true;
    }

    /**
     *
     * @param bitIndex
     * @param flag    占位符，无所谓具体置
     * @return
     */
    @Override
    public byte get(long bitIndex, boolean flag){
        return checker.readByte(bitIndex);
    }

    @Override
    public void clean(){
        checker.close();
    }

    private void initBytes(byte[] array){
        for(int i=0; i<array.length; ++i){
            array[i] = 0;
        }
    }
}
