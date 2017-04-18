#!/bin/sh

WEBVEINS_HOME=/home/shaoxiong/Code/Java/WebVeins/bin
WEBVEINS_PATH=""
for i in "$WEBVEINS_HOME"/../lib/*.jar
do
    WEBVEINS_PATH="$i:$WEBVEINS_PATH"
done
