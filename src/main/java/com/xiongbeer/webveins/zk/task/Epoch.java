package com.xiongbeer.webveins.zk.task;

import java.util.Date;

/**
 * Created by shaoxiong on 17-4-10.
 */
public class Epoch {
    private Date previousChangeTime;
    private Date checkTime;
    private String status;
    private int dataVersion;

    public long getDifference(){
        return (checkTime.getTime() - previousChangeTime.getTime())/1000;
    }

    public Epoch(long previousChangeTime, String status, int dataVersion){
        checkTime = new Date();
        this.previousChangeTime = new Date(previousChangeTime);
        this.status = status;
        this.dataVersion = dataVersion;
    }

    public int getDataVersion(){
        return dataVersion;
    }

    public void setDataVersion(int dataVersion){
        this.dataVersion = dataVersion;
    }

    public String getStatus(){
        return status;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public Date getPreviousChangeTime() {
        return previousChangeTime;
    }

    public void setPreviousChangeTime(Date previousChangeTime) {
        this.previousChangeTime = previousChangeTime;
    }

    public Date getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(Date checkTime) {
        this.checkTime = checkTime;
    }

    public String toString(){
        return "[previousChangeTime: " + previousChangeTime.toString()
                + "," + "checkTime: " + checkTime.toString()
                + "," + "timeDifference: " + getDifference() + "s" + "]";
    }
}
