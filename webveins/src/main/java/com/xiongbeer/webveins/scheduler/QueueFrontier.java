package com.xiongbeer.webveins.scheduler;

import java.util.LinkedList;

public class QueueFrontier extends Frontier{
	private Queue requestQueue = new Queue();
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
			return (String)requestQueue.remove();
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
			return (String)requestQueue.peek();
		}
	}
	
	class Queue{
		private LinkedList<Object> queue= new LinkedList<Object>();
		public void put(Object t){
			queue.addLast(t);
		}
		
		public Object remove(){
			return queue.removeFirst();
		}
		
		public Object peek(){
			return queue.getFirst();
		}
		
		public boolean isEmpty(){
			return queue.isEmpty();
		}
		
		public boolean contains(Object t){
			return queue.contains(t);
		}
		
	}
}
