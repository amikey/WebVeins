package com.xiongbeer.webveins.selector;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public class Html {
	private Document html;
	
	public Html(String entity){
		html = Jsoup.parse(entity);
	}
	
	public Element body(){
		return html.body();
	}
	
	
	public String toString(){
		return html.toString();
	}
}
