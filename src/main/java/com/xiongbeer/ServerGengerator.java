package com.xiongbeer;

import java.util.Random;

/**
 * Created by shaoxiong on 17-4-9.
 */
public class ServerGengerator {
    private String serverId = "-1";
    static private ServerGengerator server;

    static public synchronized ServerGengerator getInstance(){
        if(server == null){
            server = new ServerGengerator();
        }
        return server;
    }

    public String getServerId(){
        return serverId;
    }

    private ServerGengerator(){
        while(Integer.parseInt(serverId) < 0) {
            serverId = Integer.toString(new Random().nextInt());
        }
    }


}
