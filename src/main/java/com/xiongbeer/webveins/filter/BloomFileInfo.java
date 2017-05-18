package com.xiongbeer.webveins.filter;

import com.xiongbeer.webveins.Configuration;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bloom缓存文件的相关信息
 *
 * Created by shaoxiong on 17-5-11.
 */
public class BloomFileInfo {
    private Long urlCounter;
    private Long expectedInsertions;
    private Double fpp;
    public static final String PREFIX = "#_";
    public static final String INFIX = "#";
    public static final String SUFFIX = "_#";
    public BloomFileInfo(){}

    public BloomFileInfo(String bloomFileName) throws IOException {
        urlCounter = getUrlCounter(bloomFileName);
        expectedInsertions = getExpectedInsertions(bloomFileName);
        fpp = getFpp(bloomFileName);
    }

    public BloomFileInfo(long urlCounter
            , long expectedInsertions, double fpp){
        this.urlCounter = urlCounter;
        this.expectedInsertions = expectedInsertions;
        this.fpp = fpp;
    }

    /**
     * bloom过滤器的缓存文件的名字中包含着最大容量
     * 具体的值被 $_ 和 $ 包裹起来
     *
     * @param bloomFileName bloom缓存文件的名字
     */
    private Long getUrlCounter(String bloomFileName) throws IOException {
        Long value = null;
        Pattern pattern = Pattern.compile("(?<=\\#_)\\d*(?=\\#)");
        Matcher matcher = pattern.matcher(bloomFileName);
        while(matcher.find()){
            value = Long.parseLong(matcher.group());
            break;
        }
        if(value == null){
            throw new IOException("Invaild bloom file name: "
                    + "get UrlCounter failed");
        }
        return value;
    }

    /**
     * bloom过滤器的缓存文件的名字中包含着已经录入元素的数量
     * 具体的值被 $_ 和 $ 包裹起来
     *
     * @param bloomFileName bloom缓存文件的名字
     */
    private Long getExpectedInsertions(String bloomFileName) throws IOException {
        Long value = null;
        Pattern pattern = Pattern.compile("(?<=\\#)\\d*(?=\\#)");
        Matcher matcher = pattern.matcher(bloomFileName);
        while(matcher.find()){
            value = Long.parseLong(matcher.group());
            break;
        }
        if(value == null){
            throw new IOException("Invaild bloom file name: "
                    + "get ExpectedInsertions failed");
        }
        return value;
    }

    /**
     * bloom过滤器的缓存文件的名字中包含着误报概率
     * 具体的值被 $_ 和 $ 包裹起来
     *
     * @param bloomFileName bloom缓存文件的名字
     */
    private Double getFpp(String bloomFileName) throws IOException {
        Double value = null;
        Pattern pattern = Pattern.compile("(?<=\\#)0{1}\\.{1}\\d*(?=_\\#)");
        Matcher matcher = pattern.matcher(bloomFileName);
        while(matcher.find()){
            value = Double.parseDouble(matcher.group());
            break;
        }
        if(value == null){
            throw new IOException("Invaild bloom file name: "
                    + "get Fpp failed");
        }
        return value;
    }

    /**
     * bloom缓存文件名中包含了必要信息
     *
     * urlCounter： 目前已经存入的URL的数量
     * expectedInsertions：最大容量
     * fpp： 误报概率
     *
     * @return
     */
    @Override
    public String toString(){
        DecimalFormat df = new DecimalFormat("0.###############");
        return Configuration.BLOOM_CACHE_FILE_PREFIX
                + PREFIX + urlCounter
                + INFIX  + expectedInsertions
                + INFIX + df.format(fpp.doubleValue()) + SUFFIX
                + Configuration.BLOOM_CACHE_FILE_SUFFIX;
    }

    public Long getUrlCounter() {
        return urlCounter;
    }

    public void setUrlCounter(Long urlCounter) {
        this.urlCounter = urlCounter;
    }

    public Long getExpectedInsertions() {
        return expectedInsertions;
    }

    public void setExpectedInsertions(Long expectedInsertions) {
        this.expectedInsertions = expectedInsertions;
    }

    public Double getFpp() {
        return fpp;
    }

    public void setFpp(Double fpp) {
        this.fpp = fpp;
    }

    /**
     * TODO
     *
     * @return
     */
    public String getUniqueID(){
        return "NULL";
    }
}
