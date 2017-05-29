package com.xiongbeer.webveins.zk.task;

import java.util.Date;

/**
 * Created by shaoxiong on 17-4-10.
 */
public class Epoch {
    private final Date previousChangeTime;
    private final Date checkTime;
    private final Task.Status status;
    private final int dataVersion;

    public long getDifference(){
        return (checkTime.getTime() - previousChangeTime.getTime())/1000;
    }

    public Epoch(long previousChangeTime, Task.Status status, int dataVersion){
        checkTime = new Date();
        this.previousChangeTime = new Date(previousChangeTime);
        this.status = status;
        this.dataVersion = dataVersion;
    }

    public int getDataVersion(){
        return dataVersion;
    }

    public Task.Status getStatus(){
        return status;
    }

    public Date getPreviousChangeTime() {
        return previousChangeTime;
    }

    public Date getCheckTime() {
        return checkTime;
    }

    @Override
    public String toString(){
        return "[previousChangeTime: " + previousChangeTime.toString()
                + "," + "checkTime: " + checkTime.toString()
                + "," + "timeDifference: " + getDifference() + "s" + "]";
    }
}
