#!/bin/bash
cd `dirname $0`
BIN_DIR=`pwd`
cd ..
HOME_DIR=`pwd`
LIB_DIR=$HOME_DIR/lib
CONF_DIR=$HOME_DIR/conf
LOG_DIR=$HOME_DIR/logs
GC_DIR=$HOME_DIR/gc
DUMP_DIR=$HOME_DIR/dump
PID=`ps -f|grep java|grep $CONF_DIR|awk '{print $2}'`
if [ -n "$PID" ];
then
   echo '服务已经启动！'
   exit 1
fi
if [ ! -d $LOG_DIR ];
then
   mkdir $LOG_DIR
fi
if [ ! -d $GC_DIR ];
then
   mkdir $GC_DIR
fi
if [ ! -d $DUMP_DIR ];
then
   mkdir $DUMP_DIR
fi
STD_OUT_FILE=$LOG_DIR/stdout.log

## set java path
if [ -z "$JAVA" ] ; then
  JAVA=$(which java)
fi
JAVA_DATA=" -Dlog.dir=$LOG_DIR -Dcom.sun.management.jmxremote.port=8090 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
LIB_JARS=`ls $LIB_DIR| grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`
JAVA_MEM_OPTS=""
BITS=`java -version 2>&1 | grep -i 64-bit`
if [ -n "$BITS" ]; then
    JAVA_MEM_OPTS=" -server -Xmx2g -Xms2g -Xmn256m -XX:PermSize=128m -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70  -XX:HeapDumpPath=$DUMP_DIR/java_dump.hprof -Xloggc:$GC_DIR/gc.log -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps"
else
    JAVA_MEM_OPTS=" -server -Xms1g -Xmx1g -XX:PermSize=128m -XX:SurvivorRatio=2 -XX:+UseParallelGC -XX:HeapDumpPath=$DUMP_DIR/java_dump.hprof -Xloggc:$GC_DIR/gc.log -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps"
fi
echo "starting server....."
$JAVA $JAVA_MEM_OPTS $JAVA_DATA  -classpath $CONF_DIR:$LIB_JARS -Dlogback.home=$LOG_DIR Bootstrapper 1>>$STD_OUT_FILE 2>&1 &
if [ $?==0 ];then
   echo "服务正常启动"
else
   echo "服务异常退出"
fi
exit $?
