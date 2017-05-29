package com.xiongbeer.webveins.service.protocol;

/**
 * Created by shaoxiong on 17-5-28.
 */
public final class Message {
    public enum Coderc{
        SHORT_CON((byte)0), LONG_CON((byte)1), LOGIN_ACCEPTED((byte)2), LOGIN_REFUSED((byte)3), DUPLICATE_LOGIN((byte)4),
        LOGIN_REQ((byte)5), LOGIN_RESP((byte)6), HEART_BEAT_REQ((byte)7), HEART_BEAT_RESP((byte)8);

        private byte rc;

        Coderc(byte rc){
            this.rc = rc;
        }

        public byte getValue(){
            return rc;
        }

        public static Coderc get(byte rc){
            Coderc type = null;
            switch (rc){
                case 1:
                    break;
                case 2:
                    break;

                default:
                    break;
            }
            return type;
        }
    }
    private Header header;
    private Object body;

    public final Header getHeader(){
        return header;
    }

    public final void setHeader(Header header){
        this.header = header;
    }

    public final Object getBody(){
        return body;
    }

    public final void setBody(Object body){
        this.body = body;
    }

    @Override
    public String toString() {
        return "header:" + header + ",body:" + body;
    }
}
