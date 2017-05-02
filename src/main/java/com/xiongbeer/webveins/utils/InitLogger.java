package com.xiongbeer.webveins.utils;

/**
 * Created by shaoxiong on 17-4-9.
 */
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

public class InitLogger {
    public static void init(){
        Properties prop = new Properties();

        prop.setProperty("log4j.rootLogger", "Info,CONSOLE");
        prop.setProperty("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");
        prop.setProperty("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
        prop.setProperty("log4j.appender.CONSOLE.layout.ConversionPattern", "%d{HH:mm:ss,SSS} [%t] %-5p %C{1} : %m%n");

        PropertyConfigurator.configure(prop);
    }
}
