package com.xiongbeer.webveins.zk.task;

import com.google.common.primitives.UnsignedInteger;

/**
 * 1*byte               4*byte                 4*byte
 * 状态码            bloom标识符预留      完成度（处理完毕url的条数）
 * Created by shaoxiong on 17-6-6.
 */
public class TaskData {
    private byte[] data;
    private UnsignedInteger uniqueMarkup;
    private UnsignedInteger progress;
    private byte status;
    private static short STATUS = 8;
    private static short U_MARKUP = 4;
    private static short PROGRRESS = 0;

    public TaskData(){
        data = new byte[9];
    }

    public TaskData setStatus(byte status){
        if(status != Byte.parseByte(Task.Status.WAITING.getValue())
                && status != Byte.parseByte(Task.Status.RUNNING.getValue())
                && status != Byte.parseByte(Task.Status.FINISHED.getValue())){
            throw new IllegalArgumentException("Illegel status value");
        }
        this.status = status;
        data[STATUS] = status;
        return this;
    }

    public TaskData setProgress(int progress){
        this.progress = UnsignedInteger.fromIntBits(progress);
        for(int i=PROGRRESS; i<PROGRRESS+4; ++i){
            data[i] = (byte) (progress >> (i-PROGRRESS)*8);
        }
        return this;
    }

    public TaskData setUniqueMarkup(int uniqueMarkup){
        this.uniqueMarkup = UnsignedInteger.fromIntBits(uniqueMarkup);
        for(int i=U_MARKUP; i<U_MARKUP+4; ++i){
            data[i] = (byte) (uniqueMarkup >> (i-PROGRRESS)*8);
        }
        return this;
    }

    public UnsignedInteger getProgress(){
        return progress;
    }

    public UnsignedInteger getUniqueMarkup(){
        return uniqueMarkup;
    }

    public String getStatus(){
        return new Byte(status).toString();
    }

    public byte[] getBytes(){
        return data;
    }

    @Override
    public String toString(){
        return "[status: " + Task.Status.get(new Byte(status).toString())
                + " | uniqueMarkup: " + uniqueMarkup
                + " | progress: " + progress + ']';
    }
}
