package com.xiongbeer.webveins.exception;

/**
 * Created by shaoxiong on 17-4-15.
 */
public abstract class VeinsException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public VeinsException() {}
    public VeinsException(String message){
        super(message);
    }

    public static class FilterOverflowException extends VeinsException{
        private static final long serialVersionUID = -4566928253595479879L;
        public FilterOverflowException(){}
        public FilterOverflowException(String message){
            super(message);
        }
    }

    public static class OperationFailedException extends VeinsException{
        private static final long serialVersionUID = 3936999461191506627L;
        public OperationFailedException(){}
        public OperationFailedException(String message){
            super(message);
        }
    }
}
