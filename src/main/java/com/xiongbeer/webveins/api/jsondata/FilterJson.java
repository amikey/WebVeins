package com.xiongbeer.webveins.api.jsondata;


import java.util.Date;

/**
 * Created by shaoxiong on 17-5-15.
 */
public class FilterJson implements JData {
    /* 唯一标识符 */
    private String uniqueID;
    /* 已经存入的url的数量 */
    private long urlsNum;
    /* 文件大小 */
    private long size;
    /* 最后一次修改的时间 */
    private long mtime;
    /* 最大容量 */
    private long maxCapacity;
    /* 误差率 */
    private double fpp;

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public long getUrlsNum() {
        return urlsNum;
    }

    public void setUrlsNum(long urlsNum) {
        this.urlsNum = urlsNum;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getMtime() {
        return new Date(mtime).toString();
    }

    public void setMtime(long mtime) {
        this.mtime = mtime;
    }

    public long getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(long maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public double getFpp() {
        return fpp;
    }

    public void setFpp(double fpp) {
        this.fpp = fpp;
    }
}
