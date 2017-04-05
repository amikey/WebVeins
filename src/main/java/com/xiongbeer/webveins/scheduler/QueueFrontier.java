package com.xiongbeer.webveins.scheduler;

import com.xiongbeer.webveins.utils.Queue;

public class QueueFrontier extends Frontier{
	private Queue<String> requestQueue = new Queue<String>();
	private VisitedTable visited;
	public QueueFrontier(){
		visited = new BloomVisitedTable();
	}
	@Override
	public String next() {
		if(requestQueue.isEmpty()){
			return null;
		}
		else{
			return requestQueue.remove();
		}
	}

	@Override
	public boolean setAdd(String url) {
		if(requestQueue.contains(url)){
			return false;
		}
		
		if(visited.isExist(url) == false){
			visited.add(url);
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	public void put(String url) {
		requestQueue.put(url);
	}

	@Override
	public String get() {
		if(requestQueue.isEmpty()){
			return null;
		}
		else{
			return requestQueue.peek();
		}
	}
	
}
