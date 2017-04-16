package com.xiongbeer.filter.bloom;

import com.xiongbeer.VeinsException;

/**
 * 过滤器的用户接口
 * 封装了所有的Bloom过滤器
 * 封装了一些数学计算过程
 *
 * Created by shaoxiong on 17-4-12.
 */
public class UrlFilter {
    private double falseEPositiveRate;
    private long size;
    private long maxElementNums;
    private Bloom filter;
    private long counter;

    public enum CreateMode{
        RAM, DISK, DISK_COMPRESSED
    }

    /**
     *
     * @param elementNums 元素个数
     * @param falseEPositiveRate 错误概率
     * @param mode Bloom模式
     */
    public UrlFilter(long elementNums, double falseEPositiveRate, CreateMode mode){
        this.falseEPositiveRate = falseEPositiveRate;
        this.maxElementNums = elementNums;
        size = (long) ((-1) * elementNums * Math.log(falseEPositiveRate)
                        / (Math.pow(Math.log(2) ,2)));
        int k = (int) (Math.log(2) * size / elementNums);
        switch (mode){
            case DISK:
                filter = new DiskBloomTable(size, getPrimeNumbers(k));
                break;
            case DISK_COMPRESSED:
                filter = new CompressedBloomTable(size, getPrimeNumbers(k));
                break;
            case RAM:
                filter = new RamBloomTable(size, getPrimeNumbers(k));
                break;
        }

    }

    public boolean add(String url) throws VeinsException.FilterOverflowException {
        boolean result = false;
        if(counter < maxElementNums) {
            result = filter.add(url);
            ++counter;
        }
        else{
            throw new VeinsException.FilterOverflowException("Add new url failed.");
        }
        return result;
    }

    public boolean exist(String url){
        return filter.exist(url);
    }

    public double getFalseEPositiveRate() {
        return falseEPositiveRate;
    }

    public long getSize() {
        return size;
    }

    public long getMaxElementNums(){return maxElementNums;}

    public long getElementNums() {
        return counter;
    }

    private int[] getPrimeNumbers(int nums){
        int[] primeNumbers = new int[nums];
        for(int i=0; i<nums; ++i) {
            // hash乘数生成的表达式: 3n+1+sin(pi*n/2)^2, 生成的数比较大的概率为质数
            primeNumbers[i] = (int) (3 * i + 1 + Math.pow(Math.sin(Math.PI * i / 2), 2));
        }
        return primeNumbers;
    }
}
