package com.xiongbeer.webveins;


import com.alibaba.fastjson.JSON;
import com.google.common.io.Files;
import com.xiongbeer.webveins.api.Command;
import com.xiongbeer.webveins.api.OutputFormatter;
import com.xiongbeer.webveins.api.info.TaskInfo;
import com.xiongbeer.webveins.api.jsondata.JData;
import com.xiongbeer.webveins.api.jsondata.TaskJson;
import com.xiongbeer.webveins.utils.InitLogger;
import io.bretty.console.table.Alignment;
import io.bretty.console.table.ColumnFormatter;
import io.bretty.console.table.Precision;
import io.bretty.console.table.Table;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.fusesource.jansi.AnsiConsole;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.EnumSet;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Created by shaoxiong on 17-4-9.
 */
public class Test implements Watcher{

    public static void main(String[] args) throws IOException, InterruptedException {
        AnsiConsole.systemInstall();
        System.out.println(ansi().eraseScreen().fgRed().a("asdasdasd").reset());
    }

    @Override
    public void process(WatchedEvent watchedEvent) {

    }
}
