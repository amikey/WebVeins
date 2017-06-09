package com.xiongbeer.webveins.zk.task;

import java.util.Date;

/**
 * Created by shaoxiong on 17-4-10.
 */
public class Epoch {
    private final String taskName;
    private final Date previousChangeTime;
    private final Date checkTime;
    private final Task.Status status;
    private final int dataVersion;
    private final TaskData taskData;

    public long getDifference(){
        return (checkTime.getTime() - previousChangeTime.getTime())/1000;
    }

    public Epoch(String taskName, long previousChangeTime, int dataVersion, TaskData taskData){
        checkTime = new Date();
        this.previousChangeTime = new Date(previousChangeTime);
        this.status = taskData.getStatus();
        this.dataVersion = dataVersion;
        this.taskData = taskData;
        this.taskName = taskName;
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

    public TaskData getTaskData(){
        return taskData;
    }

    public String getTaskName(){
        return taskName;
    }

    @Override
    public String toString(){
        return  "[taskName: " + taskName +
                "previousChangeTime: " + previousChangeTime.toString()
                + "," + "checkTime: " + checkTime.toString()
                + "," + "timeDifference: " + getDifference() + "s" + "]";
    }
}
