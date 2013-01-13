app_monitor
===============

app_monitor是基于jetty嵌入式容器的java性能分析工具,内嵌H2 database
![应用性能分析图表](https://raw.github.com/langke93/app_monitor/master/doc/img/monitor_report.png)

启动服务端运行bin/restart_server.sh

客户端执行 sh performance_monitor.sh > performance_monitor.sh.log 2>&1 &
需要配置host:app.monitor.server指向服务端IP

#验证 hello servlet
http://localhost:9009/servlet/test?helloworld=this-is-jetty-embed-http

#验证 jsp
http://localhost:9009/index.jsp

#查看报表
http://localhost:9009/monitor/_report