package com.xiongbeer.filter.bloom;

/**
 * 存储在内存中的Bloom过滤器
 * TODO 加入checkPoint
 * Created by shaoxiong on 17-4-11.
 */
public class RamBloomTable extends Bloom{
    private static final int DEFAULT_SIZE = 2 << 24;
    private static final int[] DEFAULT_SEEDS = new int[]{7, 11, 13, 31, 37, 61};

    public RamBloomTable(){
        super(new BloomBitSet(DEFAULT_SIZE), DEFAULT_SIZE, DEFAULT_SEEDS);
    }

    public RamBloomTable(long size, int[] seeds){
        super(new BloomBitSet(size), size, seeds);
    }

}
