package com.xiongbeer.webveins.utils;

import org.fusesource.jansi.AnsiConsole;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Created by shaoxiong on 17-5-15.
 */
public class Color {
    public static String error(String s){
        AnsiConsole.systemInstall();
        String result = ansi().fgRed().a(s).reset().toString();
        AnsiConsole.systemUninstall();
        return result;
    }
}
