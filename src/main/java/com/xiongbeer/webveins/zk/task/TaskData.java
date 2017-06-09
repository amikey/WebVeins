package com.xiongbeer.webveins.zk.task;


/**
 *  HIGH                    --->                   LOW
 * -----------------------------------------------------------
 * 1*byte     |          4*byte      |           4*byte
 * 状态码      |      bloom标识符预留   |   完成度（处理完毕url的条数）
 * -----------------------------------------------------------
 * Created by shaoxiong on 17-6-6.
 */
public class TaskData {
    private byte[] data;
    private int uniqueMarkup;
    private int progress;
    private byte status;
    private static short STATUS = 8;
    private static short U_MARKUP = 4;
    private static short PROGRRESS = 0;

    public TaskData(){
        data = new byte[9];
    }

    public TaskData(byte[] data){
        if(data.length != 9){
            throw new IllegalArgumentException("Illegel data value");
        }
        this.data = data;
        status = data[STATUS];
        int foo = 0;
        for(int i=PROGRRESS; i<PROGRRESS+4; ++i){
            foo += (data[i] & 0xff) << (i-PROGRRESS)*8;
        }
        progress = foo;
        foo = 0;
        for(int i=U_MARKUP; i<U_MARKUP+4; ++i){
            foo += (data[i] & 0xff) << (i-U_MARKUP)*8;
        }
        uniqueMarkup =foo;
    }

    public TaskData setStatus(Task.Status status){
        byte value = Byte.parseByte(status.getValue());
        this.status = value;
        data[STATUS] = value;
        return this;
    }

    public TaskData setProgress(int progress){
        this.progress = progress;
        for(int i=PROGRRESS; i<PROGRRESS+4; ++i){
            data[i] = (byte) (progress >> (i-PROGRRESS)*8);
        }
        return this;
    }

    public TaskData setUniqueMarkup(int uniqueMarkup){
        this.uniqueMarkup = uniqueMarkup;
        for(int i=U_MARKUP; i<U_MARKUP+4; ++i){
            data[i] = (byte) (uniqueMarkup >> (i-U_MARKUP)*8);
        }
        return this;
    }

    public int getProgress(){
        return progress;
    }

    public int getUniqueMarkup(){
        return uniqueMarkup;
    }

    public Task.Status getStatus(){
        return Task.Status.get(new Byte(status).toString());
    }

    public byte[] getBytes(){
        return data;
    }

    @Override
    public String toString(){
        return "[status: " + getStatus()
                + " | uniqueMarkup: " + uniqueMarkup
                + " | progress: " + progress + ']';
    }
}
