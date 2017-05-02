package com.xiongbeer.webveins.filter.bloom;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;

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
    private int num;
    private int index;
    private ArrayList<BitSet> bitSets = new ArrayList<BitSet>();

    /*
        一个BitSet的上限为Integer.MAX_VALUE
        显然在数据量极大的情况下是不够用的。
        这里用ArrayList存放多个BitSet，而
        ArrayList的上限也为Integer.MAX_VALUE，
        那么整体的容量就扩充到了Integer.MAX_VALUE^2
     */
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

    /*
        因为有多个BisSet，这里
        需要做一下映射来定位
     */
    private void setIndex(long bitIndex){
        num = (int) (bitIndex / Integer.MAX_VALUE);
        index = (int) (bitIndex % Integer.MAX_VALUE);
    }

    @Override
    public void clean() {
        //Nothing to clean
    }

    /**
     * @deprecated
     */
    @Override
    public byte get(long bitIndex, boolean flag) {
        return 0;
    }

    /**
     * @deprecated
     */
    @Override
    public void set(long bitIndex, byte value) {}
}
