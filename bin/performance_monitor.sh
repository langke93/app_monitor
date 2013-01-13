DIRNAME=/usr/bin/dirname
BASE_PATH=`$DIRNAME $0`
cd $BASE_PATH
#export PATH=/usr/local/jdk1.6.0_26/bin:$PATH
TMPFILE=/tmp/per_mointor.tmp
IP=`/sbin/ip a |grep 'inet '|awk -F'/' '{print $1}'|awk '{print $2}'|grep -v 127.0.0.1|head -1`
JPS_FILE=/tmp/jps_file.tmp
function_send_data(){
  url="http://app.monitor.server:9009/monitor/_add"
  #echo $url "-d $body"
  curl -m 3 -E --connect-timeout=3 --compressed  -XPOST $url -d"${body}"
}

function_thread_state(){
body="["
for i in `pgrep java` ;do 
  pid=$i
  serverName=
  jstack $i |grep java.lang.Thread.State|awk '{print $2}'|sort|uniq -c >$TMPFILE 2>&1 
  while read line
  do
    # skip blank lines
    if [ "${line}" = "" ]; then
      continue
    fi
    value=`echo "${line}" | cut -d" " -f1`
    key=`echo "${line}" | cut -d" " -f2`
    serverName=`cat $JPS_FILE |grep $pid|awk '{print $2}'`
    if [ "$serverName" = "" ]; then
       continue
    fi
    #key=$serverName:$key
    #echo $serverName $key $value
    body=$body{\"server_ip\":\"$IP\",\"app_name\":\"$serverName\",\"type\":\"thread\",\"status\":\"$key\",\"val\":$value},
  done < ${TMPFILE}
done
body=$body"]"
#echo $body
function_send_data $body
}

function_network_conn(){
  body="["
  for i in `pgrep java` ;do
    pid=$i
    serverName=`cat $JPS_FILE |grep $pid|awk '{print $2}'`
    if [ "$serverName" = "" ]; then
       continue
    fi
    key="conn"
    value=`netstat -anp|awk '{print $7}'|grep "$pid/java"|awk -F/ '{print $1}'|wc -l`
    #echo $serverName $key $value
    body=$body{\"server_ip\":\"$IP\",\"app_name\":\"$serverName\",\"type\":\"network\",\"status\":\"$key\",\"val\":$value},
  done
  body=$body"]"
  #echo $body
  function_send_data $body
}

function_cpu_use(){
  body="["
  for i in `pgrep java` ;do
    pid=$i
    serverName=`cat $JPS_FILE |grep $pid|awk '{print $2}'`
    if [ "$serverName" = "" ]; then
       continue
    fi
    key="use"
    value=`ps aux|grep $serverName|grep $pid |awk '{print $3}'`
    #echo $serverName $key $value
    body=$body{\"server_ip\":\"$IP\",\"app_name\":\"$serverName\",\"type\":\"cpu\",\"status\":\"$key\",\"val\":$value},
  done
  body=$body"]"
  #echo $body
  function_send_data $body
}

while true ;do
  jps|egrep -v "Jps|Launcher" > $JPS_FILE
  function_thread_state
  function_network_conn
  function_cpu_use
  echo "sleep 60"
  sleep 60
done

#netstat -anp| awk '{print $5}' | awk -F: '{print $1}' | sort | uniq -c | sort -nr|more


