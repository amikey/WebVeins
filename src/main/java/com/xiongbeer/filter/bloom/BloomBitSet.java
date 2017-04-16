package com.xiongbeer.filter.bloom;


import java.util.ArrayList;
import java.util.BitSet;

/**
 * 扩展的BitSet
 *
 * 扩展的原因：
 * 原生一个BitSet最大位数只支持到MAX_INT
 * 所以这里将其扩充到MAX_LONG
 *
 * Created by shaoxiong on 17-4-11.
 */

public class BloomBitSet extends AbstractBitSet{
    private ArrayList<BitSet> bitSets = new ArrayList<BitSet>();
    private int num;
    private int index;

    public BloomBitSet(long size){
        int nums = (int) (size / Integer.MAX_VALUE);
        int rest = (int) (size % Integer.MAX_VALUE);
        for (int i = 0; i < nums; ++i){
            bitSets.add(new BitSet(Integer.MAX_VALUE));
        }
        bitSets.add(new BitSet(rest));
    }

    @Override
    public void set(long bitIndex, boolean value){
        setIndex(bitIndex);
        BitSet bitSet = bitSets.get(num);
        bitSet.set(index, value);
    }


    @Override
    public boolean get(long bitIndex){
        setIndex(bitIndex);
        BitSet bitSet = bitSets.get(num);
        return bitSet.get(index);
    }

    private void setIndex(long bitIndex){
        num = (int) (bitIndex / Integer.MAX_VALUE);
        index = (int) (bitIndex % Integer.MAX_VALUE);
    }

    /**
     * @deprecated
     */
    @Override
    public byte get(long bitIndex, boolean flag) {
        return 0;
    }

    @Override
    public void clean() {
        //Nothing to clean
    }

    /**
     * @deprecated
     */
    @Override
    public void set(long bitIndex, byte value) {

    }
}
