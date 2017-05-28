package com.xiongbeer.webveins;


import org.apache.hadoop.conf.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import java.util.Iterator;

/**
 * Created by shaoxiong on 17-4-9.
 */
public class Test {

    public static void main(String[] args) {
        org.apache.hadoop.conf.Configuration conf = new Configuration();
        conf.addResource(new Path("/home/shaoxiong/Code/Java/WebVeins/conf/core.xml"));
        conf.clear();
        conf.addResource(new Path("/home/shaoxiong/Code/Java/WebVeins/conf/core.xml"));
        Iterator iterator = conf.iterator();
        while(iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }
}
