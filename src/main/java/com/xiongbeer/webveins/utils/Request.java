package com.xiongbeer.webveins.utils;

public class Request {
	private int priority;
	private String url;
	
	public Request(){}
	
	public Request(String url){
		this.url = url;
	}
	
	public Request(String url, int priority){
		this.url = url;
		this.priority = priority;
	}
	
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String toString(){
		return url;
	}
}
