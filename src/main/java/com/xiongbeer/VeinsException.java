package com.xiongbeer;

/**
 * Created by shaoxiong on 17-4-15.
 */
public abstract class VeinsException extends Exception{
    public VeinsException() {}
    public VeinsException(String message){
        super(message);
    }

    public static class FilterOverflowException extends VeinsException{
        public FilterOverflowException(){}
        public FilterOverflowException(String message){
            super(message);
        }
    }
}
