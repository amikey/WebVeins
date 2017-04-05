package com.xiongbeer.webveins.utils;

import java.util.LinkedList;

public class Queue<T> {
	private LinkedList<T> queue= new LinkedList<T>();
	public void put(T t){
		queue.addLast(t);
	}
	
	public T remove(){
		return queue.removeFirst();
	}
	
	public T peek(){
		return queue.getFirst();
	}
	
	public boolean isEmpty(){
		return queue.isEmpty();
	}
	
	public boolean contains(Object t){
		return queue.contains(t);
	}
	
}
