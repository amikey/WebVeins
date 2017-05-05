#!/bin/sh
isrun=`jps | grep WebVeinsServer`
if [ "$isrun" = "" ]
then
    echo 'start server...'
else
    echo 'server has already running'
    exit 1
fi
WEBVEINS_PATH=""
for i in "$WEBVEINS_HOME"/lib/*.jar
do
    WEBVEINS_PATH="$i:$WEBVEINS_PATH"
done
WEBVEINS_PATH=$WEBVEINS_HOME/webveins.jar:$WEBVEINS_PATH

export WEBVEINS_CONF_PATH=$WEBVEINS_HOME/conf

java -cp $WEBVEINS_PATH:webveins.jar com.xiongbeer.webveins.WebVeinsServer
