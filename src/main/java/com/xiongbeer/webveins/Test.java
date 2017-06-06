package com.xiongbeer.webveins;


import com.google.common.primitives.UnsignedLong;
import com.google.common.primitives.UnsignedLongs;

/**
 * Created by shaoxiong on 17-4-9.
 */
public class Test {

    public static byte parse(long value){
        return (byte) (value >> 62);
    }

    public static long setStatus(byte value){
        return (long)value << 62;
    }

    public static void main(String[] args){
        UnsignedLong a = UnsignedLong.fromLongBits(1 << 63);
        System.out.println(Long.toString((long)1<<33));
    }
}
