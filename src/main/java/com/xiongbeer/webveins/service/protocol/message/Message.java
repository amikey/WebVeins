package com.xiongbeer.webveins.service.protocol.message;

import com.xiongbeer.webveins.service.ProcessDataProto;

/**
 * Created by shaoxiong on 17-5-28.
 */
public final class Message {
    public enum Coderc{
        LOGIN_REFUSED((byte)1), DUPLICATE_LOGIN((byte)2), HEART_BEAT_REQ((byte)3), HEART_BEAT_RESP((byte)4), CRAWLER_REQ((byte)5),
        CRAWLER_RESP((byte)6), SHELL_REQ((byte)7), SHELL_RESP((byte)8), LOGIN_REQ((byte)9), LOGIN_RESP((byte)10);

        private byte rc;

        Coderc(byte rc){
            this.rc = rc;
        }

        public byte getValue(){
            return rc;
        }

        public static Coderc get(byte rc){
            switch (rc){
                case 1:
                    return LOGIN_REFUSED;
                case 2:
                    return DUPLICATE_LOGIN;
                case 3:
                    return HEART_BEAT_REQ;
                case 4:
                    return HEART_BEAT_RESP;
                case 5:
                    return CRAWLER_REQ;
                case 6:
                    return CRAWLER_RESP;
                case 7:
                    return SHELL_REQ;
                case 8:
                    return SHELL_RESP;
                default:
                    return null;
            }
        }
    }
    private Header header;
    private ProcessDataProto.ProcessData body;

    public final Header getHeader(){
        return header;
    }

    public final void setHeader(Header header){
        this.header = header;
    }

    public final ProcessDataProto.ProcessData getBody(){
        return body;
    }

    public final void setBody(ProcessDataProto.ProcessData body){
        this.body = body;
    }

    @Override
    public String toString() {
        return "header:" + header + ",body:" + body;
    }
}
