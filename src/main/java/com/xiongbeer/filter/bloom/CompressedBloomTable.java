package com.xiongbeer.filter.bloom;

import com.xiongbeer.Configuration;

/**
 * 经过压缩的持久化在磁盘的Bloom过滤器
 *
 * Created by shaoxiong on 17-4-11.
 */
public class CompressedBloomTable extends BloomTable{
    private static final long DEFAULT_SIZE = 2 << 24;
    private static final int[] DEFAULT_SEEDS = new int[]{7, 11, 13, 31, 37, 61};

    public CompressedBloomTable(){
        super(new DiskBloomBitSet(DEFAULT_SIZE/8+1, Configuration.C_BLOOM_SAVE_PATH),
                                    DEFAULT_SIZE, DEFAULT_SEEDS);
    }

    public CompressedBloomTable(long size, int[] seeds){
        super(new DiskBloomBitSet(size/8+1, Configuration.C_BLOOM_SAVE_PATH), size, seeds);
    }

    @Override
    public boolean add(String url){
        if(url != null) {
            if(!exist(url)) {
                long temp, position;
                byte offset, stat;
                for (HashMaker f : super.func) {
                    temp = f.getHash(url);
                    position = temp / 8;
                    offset = (byte) (temp % 8);
                    stat = super.bits.get(position, true);
                    super.bits.set(position, (byte) (stat | (1 << offset)));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean exist(String url){
        if(url == null){
            return false;
        }
        byte value, offset;
        long temp, position;
        boolean ret = true;
        for(HashMaker f:super.func){
            temp = f.getHash(url);
            position = temp/8;
            offset = (byte) (temp%8);
            value = super.bits.get(position, true);
            ret = ret && ((value & 1<<offset) == 0?false:true);
        }
        return ret;
    }

    public void clean(){
        bits.clean();
    }
}
