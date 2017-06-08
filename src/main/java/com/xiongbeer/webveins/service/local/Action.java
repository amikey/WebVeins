package com.xiongbeer.webveins.service.local;

import com.google.common.primitives.UnsignedInteger;

/**
 * Created by shaoxiong on 17-4-26.
 */
public interface Action {

    boolean run(String urlFilePath);

    UnsignedInteger report();

    void reportResult(UnsignedInteger result);
}
