package com.xiongbeer.webveins.scheduler;

import java.util.BitSet;

/**
 * 
 * @author shaoxiong
 *
 */

public class BloomVisitedTable extends VisitedTable{
	private static final int DEFAULT_SIZE = 2 << 24;
	private int size;
	private static final int[] DEFAULT_SEEDS = new int[]{7, 11, 13, 31, 37, 61};
	private int[] seeds;
	private BitSet bits;
	private HashMaker[] func;
	
	private void init(){
		bits = new BitSet(size);
		func = new HashMaker[seeds.length];
		for(int i=0; i<seeds.length; ++i){
			func[i] = new HashMaker(size, seeds[i]);
		}
	}	
	
	public BloomVisitedTable(){
		size = DEFAULT_SIZE;
		seeds = DEFAULT_SEEDS;
		init();
	}
	
	public BloomVisitedTable(int size, int[] seeds){
		this.size = size;
		this.seeds = seeds;
		init();
	}
	

	
	@Override
	public boolean isExist(String url) {
		if(url == null){
			return false;
		}
		boolean ret = true;
		for(HashMaker f:func){
			ret = ret && bits.get(f.getHash(url));
		}
		return ret;
	}

	@Override
	public void add(String url) {
		for(HashMaker f:func){
			bits.set(f.getHash(url), true);
		}
	}
	
	public static class HashMaker{
		private int cap;
		private int seed;
		
		public HashMaker(int cap, int seed){
			this.cap = cap;
			this.seed = seed;
		}
		
		public int getHash(String value){
			int result = 0;
			int len = value.length();
			for(int i=0; i<len; ++i){
				result = seed*result + value.charAt(i);
			}
			return (cap - 1) & result;
		}
	}
	
}
