package com.xiongbeer.webveins.service.local;

import com.xiongbeer.webveins.service.protocol.Client;

/**
 * Created by shaoxiong on 17-6-2.
 */
public abstract class Bootstrap {
    protected Client client;

    abstract public void ready();

    public Bootstrap init(){
        client.connect();
        return this;
    }

    public Bootstrap close() {
        client.disconnect();
        return this;
    }
}
