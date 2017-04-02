package com.xiongbeer.webveins.scheduler;

public abstract class Frontier {
	public abstract String next();
	public abstract boolean setAdd(String url);
	public abstract void put(String url);
	public abstract String get();
}
