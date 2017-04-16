package com.xiongbeer.filter.bloom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 从文件中随机读写某个Byte
 * TODO Mapbuffer的不定时写入问题
 * Created by shaoxiong on 17-4-11.
 */
public class ByteChecker {
    private File file;
    private RandomAccessFile op;
    private FileChannel channel;

    public ByteChecker(File file){
        this.file = file;
        open();
    }

    public void open(){
        try {
            op = new RandomAccessFile(file, "rw");
            channel = op.getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close(){
        try {
            channel.close();
            op.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * NativeIO 随机写
     * @param position
     * @param value
     */
    public void setByte(long position, byte value){
        try{
            op.seek(position);
            op.writeByte(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * NativeIO 随机读
     * @param position
     * @return
     */
    public Byte readByte(long position){
        try{
            op.seek(position);
            return op.readByte();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * MappedBuffer NIO 随机写
     * @param position
     * @param value
     */
    public void setByteMapped(long position, byte value){
        try {
            MappedByteBuffer mapBuffer = channel.map(FileChannel.MapMode.READ_WRITE, position, 1);
            mapBuffer.put(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * MappedBuffer NIO 随机读
     * @param position
     * @return
     */
    public Byte readByteMapped(long position){
        try {
            MappedByteBuffer mapBuffer = channel.map(FileChannel.MapMode.READ_WRITE, position, 1);
            return mapBuffer.get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
