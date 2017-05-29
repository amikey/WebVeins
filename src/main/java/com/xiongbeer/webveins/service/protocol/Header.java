package com.xiongbeer.webveins.service.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shaoxiong on 17-5-28.
 */
public class Header {
    private int crcCode = 0xbcda2431;
    private int length;
    private long sessionId;
    private byte type;
    private byte priority;
    private Map<String, Object> attachment = new HashMap<String, Object>();

    public final int getCrcCode(){
        return crcCode;
    }

    public final void setCrcCode(int crcCode){
        this.crcCode = crcCode;
    }

    public final int getLength() {
        return length;
    }

    public final void setLength(int length) {
        this.length = length;
    }

    public final long getSessionID() {
        return sessionId;
    }

    public final void setSessionID(long sessionId) {
        this.sessionId = sessionId;
    }

    public final byte getType() {
        return type;
    }

    public final void setType(byte type) {
        this.type = type;
    }

    public final byte getPriority() {
        return priority;
    }

    public final void setPriority(byte priority) {
        this.priority = priority;
    }

    public final Map<String, Object> getAttachment() {
        return attachment;
    }

    public final void setAttachment(Map<String, Object> attachment) {
        this.attachment = attachment;
    }

    @Override
    public String toString() {
        return "Header [crcCode=" + crcCode + ", length=" + length
                + ", sessionId=" + sessionId + ", type=" + type + ", priority="
                + priority + ", attachment=" + attachment + "]";
    }
}
