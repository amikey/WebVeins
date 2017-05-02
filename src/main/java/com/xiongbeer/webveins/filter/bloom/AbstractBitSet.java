package com.xiongbeer.webveins.filter.bloom;

/**
 * Created by shaoxiong on 17-4-11.
 */
abstract public class AbstractBitSet {
    abstract public void set(long bitIndex, boolean value);
    abstract public void set(long bitIndex, byte value);
    abstract public boolean get(long bitIndex);
    abstract public byte get(long bitIndex, boolean flag);
    abstract public void clean();
}
