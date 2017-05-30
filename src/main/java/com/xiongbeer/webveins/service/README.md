消息定义

    名称      类型       长度 
    header    Header    变长
    body      protobuf  变长
    
消息头定义

    名称         类型                   长度
    crcCode     int                     32
    length      int                     32
    sessionId   long                    64
    type        byte                    8
    priority    byte                    8
    attachment  Map<String,Object>     变长
    
消息体定义

    名称          类型                  长度
    