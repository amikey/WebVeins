package com.xiongbeer.service;

import java.io.Serializable;

/**
 * Created by shaoxiong on 17-4-23.
 */
public class ServerData implements Comparable<ServerData>, Serializable {
    private static final long serialVersionUID = -4600985135561116891L;

    /* 负载数量 */
    private Integer load;

    private int port;
    private String host;

    public ServerData(String host, int port){
        load = new Integer(0);
        this.host = host;
        this.port = port;
    }

    public void addLoad(int step){
        this.load += step ;
    }

    public void reduceLoad(int step){
        this.load = Math.max(this.load - step, 0);
    }

    public Integer getLoad() {
        return load;
    }

    public void setLoad(Integer load) {
        this.load = load;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String toString(){
        return "ServerData [load=" + load + ", host="
                + host + ", port=" + port + "]";
    }

    @Override
    public int compareTo(ServerData o) {
        return this.load.compareTo(o.getLoad());
    }
}
