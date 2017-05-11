package com.xiongbeer.webveins.exception;

/**
 * Created by shaoxiong on 17-4-15.
 */
public abstract class VeinsException extends Exception{
	private static final long serialVersionUID = 1L;

	public VeinsException() {}
    public VeinsException(String message){
        super(message);
    }

    public static class FilterOverflowException extends VeinsException{
		private static final long serialVersionUID = 1L;
		public FilterOverflowException(){}
        public FilterOverflowException(String message){
            super(message);
        }
    }
}
