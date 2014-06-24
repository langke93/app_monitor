#!/bin/sh
if [ "`whoami`" != "root" ] ; then
        echo
#        echo Must start use root
        echo
#        exit 1
fi

DIRNAME=/usr/bin/dirname
BASE_PATH=`$DIRNAME $0`/..
cd $BASE_PATH
echo `/bin/pwd`

#export JAVA_HOME=/usr/local/jdk1.6.0_26
#export PATH=/usr/local/jdk1.6.0_26/bin:$PATH

CLASSPATH=$BASE_PATH/conf/:$JAVA_HOME/lib/dt.jar:/$JAVA_HOME/lib/tools.jar:$CLASSPATH
for i in bin/*.jar ;do
 CLASSPATH=$CLASSPATH:$i;
done
for i in lib/*.jar ;do
 CLASSPATH=$CLASSPATH:$i;
done

SEARCH_VER=9
#DEFAULT_OPTS="-server -Xms100M -Xmx500M"
DEFAULT_OPTS="$DEFAULT_OPTS -Dcom.sun.management.jmxremote.port=89${SEARCH_VER}6" 
DEFAULT_OPTS="$DEFAULT_OPTS -Dcom.sun.management.jmxremote.authenticate=false"
DEFAULT_OPTS="$DEFAULT_OPTS -Dcom.sun.management.jmxremote.ssl=false"
DEBUG_INFO=" -Xdebug -Xrunjdwp:transport=dt_socket,address=1527${SEARCH_VER},server=y,suspend=n "
DEBUG=""
case $1 in
        "debug") DEBUG=${DEBUG_INFO};;
        esac;
shift;
PNAME=org.langke.jetty.server.AppMonitorJettyServer
if test $(pgrep -f ${PNAME}|wc -l) -ne 0;then
  echo "closing...... $PNAME"
  pkill -f $PNAME
  sleep 1
fi


# process
CMD="java -cp $CLASSPATH $DEFAULT_OPTS $DEBUG ${PNAME}  > /dev/null 2>&1 &"
eval $CMD
echo "start ~~ $CMD"
echo "as pid:`pgrep -f ${PNAME}`"
