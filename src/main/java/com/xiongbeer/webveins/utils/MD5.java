package com.xiongbeer.webveins.utils;

import java.math.BigInteger;
import java.security.MessageDigest;

public class MD5 {
    private String key;
    public MD5(String key){
        this.key = key;
    }

    public String getMD5(){
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(key.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}