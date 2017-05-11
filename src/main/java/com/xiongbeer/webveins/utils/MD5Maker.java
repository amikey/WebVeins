package com.xiongbeer.webveins.utils;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import java.nio.charset.Charset;

/**
 * Created by shaoxiong on 17-5-10.
 */
public class MD5Maker {
    private Hasher hash;
    private String value;
    public MD5Maker(){
        value = null;
        hash = Hashing.md5().newHasher();
    }

    public MD5Maker(CharSequence charSequence){
        value = null;
        hash = Hashing.md5().newHasher();
        hash.putString(charSequence, Charset.defaultCharset());
    }

    public void update(CharSequence charSequence){
        if(value == null) {
            hash.putString(charSequence, Charset.defaultCharset());
        }
    }

    public void reset(){
        hash = Hashing.md5().newHasher();
        value = null;
    }

    /**
     * 注意，一旦调用该方法后就MD5的值就被确定了
     * 也就是说可以再调用update不会有任何的效果
     * 可以通过reset方法清空之前的所有信息
     *
     * @return
     */
    @Override
    public String toString(){
        if(value == null){
            value = hash.hash().toString();
        }
        return value;
    }
}
