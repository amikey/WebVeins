package com.xiongbeer.webveins.check;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by shaoxiong on 17-5-6.
 */
public class SelfTest {

    /**
     * 检查某个class是否已经在运行
     *
     * @param className
     * @return
     */
    public static boolean check(String className){
        boolean result = false;
        int counter = 0;
        try {
            Process process = Runtime.getRuntime().exec("jps");
            InputStreamReader iR = new InputStreamReader(process.getInputStream());
            BufferedReader input = new BufferedReader(iR);
            String line;
            while((line = input.readLine()) != null){
                if(line.matches(".*"+className)){
                    counter++;
                    if(counter > 1) {
                        result = true;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
