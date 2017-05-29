package com.xiongbeer.webveins;


import com.google.common.base.Optional;
import com.xiongbeer.webveins.zk.task.Task;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import java.util.Iterator;

/**
 * Created by shaoxiong on 17-4-9.
 */
public class Test {
    public static void main(String[] args) {
        Optional<Integer> possible  = Optional.fromNullable(1);
        System.out.println(possible.get());
    }
}
