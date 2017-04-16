package com.xiongbeer;

/**
 * Created by shaoxiong on 17-4-10.
 * TODO:改为位运算
 */
public class Tracker {
    public static final int WAITING    = 3;
    public static final int FINNISHED  = 1;
    public static final int SUCCESS    = 0;
    public static final int FAILED     = 2;
    private int status;
    public Tracker(){
        status = WAITING;
    }

    public void setStatus(int status){
        this.status = status;
    }

    public void andStatus(int status){

    }

    public int getStatus(){
        return status;
    }

    public String toString(){
        String result = "";
        switch (status){
            case WAITING:
                result = "WAITING";
                break;
            case FAILED:
                result =  "FAILED";
                break;
            case FINNISHED:
                result = "FINNISHED";
                break;
            case SUCCESS:
                result = "SUCCESS";
                break;
        }
        return result;
    }
}
