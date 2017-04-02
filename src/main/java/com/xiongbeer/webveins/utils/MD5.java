package com.xiongbeer.webveins.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MD5 {
	private String key;
	public MD5(){}
	public MD5(String key){
		this.key = key;
	}
	
	public String getMD5() throws Exception{
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(key.getBytes());
			return new BigInteger(1, md.digest()).toString(16);
		} catch (Exception e) {
			throw new Exception("Get MD5 failed");
		}
	}
}
