package com.xiongbeer.webveins.filter;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.xiongbeer.webveins.Configuration;
import com.xiongbeer.webveins.utils.MD5Maker;

import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shaoxiong on 17-5-10.
 */
public class UrlFilter {
    private BloomFilter<CharSequence> bloomFilter;
    private AtomicLong urlCounter;
    private double fpp;
    private long expectedInsertions;

    /**
     * 初始化一个bloom过滤器到内存中
     *
     * @param expectedInsertions 预估的最大元素容量
     * @param fpp 误报概率
     */
    public UrlFilter(long expectedInsertions, double fpp){
        urlCounter = new AtomicLong(0);
        this.expectedInsertions = expectedInsertions;
        this.fpp = fpp;
        bloomFilter = BloomFilter.create(
                Funnels.stringFunnel(Charset.defaultCharset()), expectedInsertions, fpp);
    }

    /**
     * 读取持久化的bloom缓存文件来初始化
     *
     * 默认当前只存在一个bloom缓存文件
     * 将其读取到内存中来
     *
     * @param cachePath 存取缓存文件的文件夹路径
     * @throws IOException
     */
    public UrlFilter(String cachePath) throws IOException {
        File cacheDir = new File(cachePath);
        File file = new File(getBloomFileName(cacheDir));
        BloomFileInfo info = new BloomFileInfo(file.getName());
        urlCounter = new AtomicLong(info.getUrlCounter().longValue());
        expectedInsertions = info.getExpectedInsertions().longValue();
        fpp = info.getFpp().doubleValue();
        load(file.getAbsolutePath());
    }

    /**
     * 读取持久化的bloom缓存文件来初始化
     *
     * 读取某个特定的名字的bloom缓存文件
     *
     * @param cachePath 存取缓存文件的文件夹路径
     * @param uniqueMarkupRegex 能捕获包含唯一标识符的bloom缓存文件的正则表达式
     * @throws IOException
     */
    public UrlFilter(String cachePath, String uniqueMarkupRegex) throws IOException {
        File cacheDir = new File(cachePath);
        String bloomFileName = getBloomFileName(cacheDir, uniqueMarkupRegex);
        File file = new File(cachePath + File.separator + bloomFileName);
        BloomFileInfo info = new BloomFileInfo(bloomFileName);
        urlCounter = new AtomicLong(info.getUrlCounter().longValue());
        expectedInsertions = info.getExpectedInsertions().longValue();
        fpp = info.getFpp().doubleValue();
        load(file.getAbsolutePath());
    }

    /**
     * 获取bloom缓存文件名
     *
     * 获取规则：以Configuration中预先设置的BLOOM_CACHE_FILE_SUFFIX结尾
     *
     * @param dir 读取的文件夹
     * @return 唯一的bloom缓存文件名字
     * @throws IOException 当读取的路径下没有或者有多于一个
     *                      bloom缓存文件的时候就会抛出异常
     */
    public static String getBloomFileName(File dir) throws IOException {
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if(pathname.toString().endsWith(
                        Configuration.BLOOM_CACHE_FILE_SUFFIX)){
                    return true;
                }
                return false;
            }
        });
        if(files.length != 1){
            throw new IOException("No bloom cache file exist or duplicate bloom files");
        }
        return files[0].getAbsolutePath();
    }

    /**
     * 获取bloom缓存文件名
     *
     * 获取规则：用户自己定义的唯一标识符的正则表达式
     * tip: 用户应该提前用Java自带的正则库写个小程序
     *      来测试自己定义的规则是否能正常获取
     *
     * @param dir
     * @param uniqueMarkupRegex
     * @return 唯一的bloom缓存文件名字
     * @throws IOException 没有获取到或者获取到多个本该是唯一
     *                      bloom缓存文件的时候就会抛出异常
     */
    public static String getBloomFileName(File dir, final String uniqueMarkupRegex) throws IOException {
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                Pattern pattern = Pattern.compile(uniqueMarkupRegex);
                Matcher matcher = pattern.matcher(pathname.toString());
                while(matcher.find()){
                    return true;
                }
                return false;
            }
        });
        if(files.length > 1){
            throw new IOException("Duplicate unique bloom files, uniqueMarkup: "
                    + uniqueMarkupRegex);
        }
        else if(files.length == 0){
            throw new IOException("No such unique bloom cache file, uniqueMarkup:"
                    + uniqueMarkupRegex);
        }
        return files[0].getAbsolutePath();
    }

    /**
     * 持久化bloom过滤器在内存中的状态
     *
     * @param targetDir 存储的文件夹
     * @throws IOException
     * @return 保存文件的绝对路径
     */
    public String save(String targetDir) throws IOException {
        BloomFileInfo info = new BloomFileInfo(
                urlCounter.longValue(),
                expectedInsertions, fpp);
        String newName = targetDir + File.separator + info.toString();
        File file = new File(newName);
        FileOutputStream fos = new FileOutputStream(file);
        bloomFilter.writeTo(fos);
        return file.getAbsolutePath();
    }

    /**
     * 从本地读取某个bloom过滤器的数据
     *
     * @param path
     * @throws IOException
     */
    public void load(String path) throws IOException {
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        bloomFilter = BloomFilter.readFrom(fis, Funnels.stringFunnel(Charset.defaultCharset()));
    }

    /**
     * 将某个url存入bloom过滤器中
     *
     * 这里使用对url取MD5的原因是这样可以去贴近理论的fpp值
     * 直接裸用url的话，很可能导致实际fpp值比预设值的fpp更高
     *
     * @param url
     */
    public boolean put(String url){
        boolean flag = false;
        MD5Maker md5 = new MD5Maker(url);
        String md5Value = md5.toString();
        synchronized (this) {
            flag = bloomFilter.put(md5Value);
        }
        if(flag){
            urlCounter.incrementAndGet();
        }
        return flag;
    }

    /**
     * 判断是否可能包含某个url
     *
     * @param url
     * @return true url可能已经存在
     *         false url一定不存在
     */
    public boolean mightContain(String url){
        MD5Maker md5 = new MD5Maker(url);
        return bloomFilter.mightContain(md5.toString());
    }

    public long getUrlCounter() {
        return urlCounter.longValue();
    }

    public double getFpp() {
        return fpp;
    }

    public long getExpectedInsertions() {
        return expectedInsertions;
    }
}
