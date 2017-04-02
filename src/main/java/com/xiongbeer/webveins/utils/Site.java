package com.xiongbeer.webveins.utils;

import java.util.HashMap;
import java.util.Map;

public class Site {
	private String charset;
	private String userAgent;
	private String cookie;
	private String domain;
	private Map<String, String> headers = new HashMap<String ,String>(); 
	
	public Site(){}

	
	public Map<String, String> getHeaders(){
		return this.headers;
	}
	
	public Site addHeader(String key, String value){
		this.headers.put(key, value);
		return this;
	}
	
	public String getCharset() {
		return charset;
	}

	public Site setCharset(String charset) {
		this.charset = charset;
		return this;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public Site setUserAgent(String userAgent) {
		this.userAgent = userAgent;
		return this;
	}

	public String getCookie() {
		return cookie;
	}

	public Site setCookie(String cookie) {
		this.cookie = cookie;
		return this;
	}

	public String getDomain() {
		return domain;
	}

	public Site setDomain(String domain) {
		this.domain = domain;
		return this;
	}
	
	
}
