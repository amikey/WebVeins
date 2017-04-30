package com.xiongbeer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * 根据本机的IPv4地址来提供命名服务
 *
 * Created by shaoxiong on 17-4-30.
 */
public class IdProvider {
    private Logger logger = LoggerFactory.getLogger(IdProvider.class);
    public String getId(){
        String id="-1";
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address) {
                            id=ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Cannot get Id, something wrong with getting Ipv4 address");
        }
        return id;
    }
}
