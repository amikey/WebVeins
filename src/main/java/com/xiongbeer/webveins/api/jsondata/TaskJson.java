package com.xiongbeer.webveins.api.jsondata;

import com.xiongbeer.webveins.zk.task.Task;

import java.util.Date;

/**
 * Created by shaoxiong on 17-5-12.
 */
public class TaskJson implements JData {
    /* 创建时间 */
    private long cTime;
    /* 最后一次修改时间 */
    private long mTime;
    /* 路径 */
    private String name;
    /* 状态 */
    private String status;

    public String getCtime() {
        return new Date(cTime).toString();
    }

    public void setCtime(long cTime) {
        this.cTime = cTime;
    }

    public String getMtime() {
        return new Date(mTime).toString();
    }

    public void setMtime(long mTime) {
        this.mTime = mTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        String result = "Unknow";
        switch (status) {
            case Task.WAITING:
                result = "WAITING";
                break;
            case Task.RUNNING:
                result = "RUNNING";
                break;
            case Task.FINISHED:
                result = "FINISHED";
                break;
            default:
                break;
        }
        return result;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
