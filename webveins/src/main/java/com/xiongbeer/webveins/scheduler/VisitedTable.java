package com.xiongbeer.webveins.scheduler;

public abstract class VisitedTable {
	public abstract boolean isExist(String url);
	public abstract void add(String url);
}
