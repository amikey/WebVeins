package com.xiongbeer.filter.bloom;

import com.xiongbeer.MD5;
import org.junit.Test;

import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by shaoxiong on 17-4-11.
 */
public class BloomRunTimeTest {
    final long SIZE = (long)Math.pow(2, 24);
    final int[] SEEDS = new int[]{7, 11, 13, 31, 37, 61};
    RamBloomTable ramBloom = new RamBloomTable();
    DiskBloomTable diskBloom = new DiskBloomTable(SIZE, SEEDS);
    CompressedBloomTable comBloom = new CompressedBloomTable(SIZE, SEEDS);

    @Test
    public void runtimeTest(){
        int nums = 1000;
        LinkedList<String> testStrs = randomStr(nums);
        long start1 = System.currentTimeMillis();
        for(String str:testStrs){
            ramBloom.add(str);
        }
        long start2 =  System.currentTimeMillis();
        for(String str:testStrs){
            ramBloom.exist(str);
        }
        long start3 = System.currentTimeMillis();
        for(String str:testStrs){
            diskBloom.add(str);
        }
        long start4 = System.currentTimeMillis();
        for(String str:testStrs){
            diskBloom.exist(str);
        }
        long start5 = System.currentTimeMillis();
        for(String str:testStrs){
            comBloom.add(str);
        }
        long start6 = System.currentTimeMillis();
        for(String str:testStrs){
            comBloom.exist(str);
        }
        long end = System.currentTimeMillis();
        diskBloom.clean();
        comBloom.clean();
        System.out.println("ram add: "  + (start2-start1)   + '\n' +
                           "ram is: "   + (start3-start2)   + '\n' +
                           "disk add: " + (start4-start3)   + '\n' +
                           "disk is: "  + (start5-start4)   + '\n' +
                           "com add: "  + (start6-start5)   + '\n' +
                           "com is: "   + (end-start1));

    }

    public LinkedList<String> randomStr(int nums){
        LinkedList<String> result = new LinkedList<String>();
        String temp;
        Random rand = new Random();
        for(int i=0; i<nums; ++i) {
            temp = new Float(rand.nextFloat()).toString();
            MD5 md = new MD5(temp);
            result.add(md.getMD5());
        }
        return result;
    }
}
