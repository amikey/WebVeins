package com.xiongbeer.webveins.utils;

import com.xiongbeer.webveins.utils.Content;
import com.xiongbeer.webveins.utils.Html;

public class Page {
	private Html html;
	private int statusCode;
	private Content content;
	
	public Page(){}
	
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
