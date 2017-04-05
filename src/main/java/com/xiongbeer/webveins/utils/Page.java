package com.xiongbeer.webveins.utils;



import com.xiongbeer.webveins.selector.Html;
import com.xiongbeer.webveins.utils.Content;

public class Page {
	private Html html;
	private int statusCode;
	private Content content;
    private Queue<Request> targetRequests = new Queue<Request>();
	
	public Page(){}
	
	
	public void addTargetRequests(String url){
		targetRequests.put(new Request(url));
	}
	
	public void addTargetRequests(String url, int priority){
		targetRequests.put(new Request(url, priority));
	}
	
	public Request getNextTargetRequest(){
		if(targetRequests.isEmpty())
			return null;
		return targetRequests.remove();
	}
		
	public Html getHtml(){
		return this.html;
	}
	
	public Content getContent(){
		return this.content;
	}
	
	public void setStatusCode(int statusCode){
		this.statusCode = statusCode;
	}
	
	public void setHtml(String entity){
		this.html = new Html(entity);
	}
	
	public int getStatusCode(){
		return this.statusCode;
	}
}
