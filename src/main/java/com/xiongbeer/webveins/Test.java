package com.xiongbeer.webveins;

import javax.swing.text.html.Option;
import java.io.File;
import java.util.*;

/**
 * Created by shaoxiong on 17-4-9.
 */
public class Test {

    public static void main(String[] args){
        File dir = new File("/");
        Optional<File[]> files = Optional.ofNullable(dir.listFiles());
        files.ifPresent(list -> Arrays.asList(list).forEach(System.out::println));
    }
}
