#!/bin/sh
echo 'start server...'
WEBVEINS_PATH=""
for i in "$WEBVEINS_HOME"/lib/*.jar
do
    WEBVEINS_PATH="$i:$WEBVEINS_PATH"
done
WEBVEINS_PATH=$WEBVEINS_HOME/webveins.jar:$WEBVEINS_PATH

export WEBVEINS_CONF_PATH=$WEBVEINS_HOME/conf

java -cp $WEBVEINS_PATH:webveins.jar com.xiongbeer.webveins.WebVeinsServer
