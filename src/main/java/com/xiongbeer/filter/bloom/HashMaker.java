package com.xiongbeer.filter.bloom;

/**
 * Created by shaoxiong on 17-4-11.
 */
public class HashMaker {
    private long cap;
    private int seed;

    public HashMaker(long cap, int seed){
        this.cap = cap;
        this.seed = seed;
    }

    public long getHash(String value){
        long result = 0;
        int len = value.length();
        for(int i=0; i<len; ++i){
            result = seed*result + value.charAt(i);
        }
        return (cap - 1) & result;
    }
}
