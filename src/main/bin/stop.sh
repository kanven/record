#!/bin/bash
cd `dirname $0`
BIN_DIR=`pwd`
cd ..
HOME_DIR=`pwd`
CONF_DIR=$HOME_DIR/conf
PID=`ps -f|grep java|grep $CONF_DIR|awk '{print $2}'`
if [ -n "$PID" ];
then
   kill -9 $PID
fi
exit 0