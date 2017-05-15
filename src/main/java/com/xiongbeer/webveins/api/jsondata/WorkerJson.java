package com.xiongbeer.webveins.api.jsondata;

/**
 * Created by shaoxiong on 17-5-12.
 */
public class WorkerJson implements JData {
    /* 名称 */
    private String name;
    /* 当前执行的任务 */
    private String currentTask;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(String currentTask) {
        this.currentTask = currentTask;
    }
}
