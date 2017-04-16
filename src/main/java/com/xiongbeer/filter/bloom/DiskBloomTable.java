package com.xiongbeer.filter.bloom;

import com.xiongbeer.Configuration;

/**
 * 持久化在磁盘的Bloom过滤器
 *
 * Created by shaoxiong on 17-4-11.
 */
public class DiskBloomTable extends  Bloom{
    private static final long DEFAULT_SIZE = 2 << 24;
    private static final int[] DEFAULT_SEEDS = new int[]{7, 11, 13, 31, 37, 61};

    public DiskBloomTable(){
        super(new DiskBloomBitSet(DEFAULT_SIZE, Configuration.BLOOM_SAVE_PATH),
                                    DEFAULT_SIZE, DEFAULT_SEEDS);
    }

    public DiskBloomTable(long size, int[] seeds){
        super(new DiskBloomBitSet(size, Configuration.BLOOM_SAVE_PATH), size, seeds);
    }

    public void clean(){
        bits.clean();
    }
}
