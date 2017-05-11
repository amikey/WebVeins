package com.xiongbeer.webveins;


import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

/**
 * Created by shaoxiong on 17-4-9.
 */
public class Test {

    public static void main(String[] args) throws IOException {
        long time = System.currentTimeMillis();
        StringBuffer buffer = new StringBuffer();
        for(int i=0; i<1001; ++i) {
           buffer.append("123");
        }
    }

}
