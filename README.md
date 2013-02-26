app_monitor
===============

app_monitor是基于jetty嵌入式容器的java性能分析工具,内嵌H2 database,以图表形式直观展现当前应用的性能数据
![应用性能分析图表](https://raw.github.com/langke93/app_monitor/master/doc/img/performanceAnalysis.png)(https://raw.github.com/langke93/app_monitor/master/doc/img/monitor__report.png)

部署目录结构：
\bin
\lib
\src
\conf
启动服务端运行bin/restart_server.sh

客户端执行 sh performance_monitor.sh > performance_monitor.sh.log 2>&1 &
需要配置host:app.monitor.server指向服务端IP

#验证 hello servlet
http://localhost:9009/servlet/test?helloworld=this-is-jetty-embed-http

#验证 jsp
http://localhost:9009/index.jsp

#查看报表
http://localhost:9009/monitor/_report

#2013.1.20
整合UI、增加菜单管理

#1.javamelody集成Application
启动main函数加：
	new JavaMelodyMonitorServer(o.serverName(),o.getServerAddress().getHost(),o.getServerAddress().getPort());

spring 加：
    	<aop:aspectj-autoproxy/> 
	<!-- javamelody  -->
	<bean id="facadeMonitoringAdvisor" class="net.bull.javamelody.MonitoringSpringAdvisor">
	      <property name="pointcut">
	              <bean class="org.springframework.aop.support.JdkRegexpMethodPointcut">
	                      <property name="pattern" value="org.langke.*" />
	              </bean>
	      </property>
    </bean>		
	<bean class="org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor"/>

	<bean id="springDataSourceBeanPostProcessor" class="net.bull.javamelody.SpringDataSourceBeanPostProcessor">
 	</bean>
	
 	<bean id="wrappedDataSource" class="net.bull.javamelody.SpringDataSourceFactoryBean">
		<property name="targetName" value="dataSource" />
	</bean>
	<!-- javamelody  -->
		
需要的jar包
javamelody.jar
jetty-6.1.26.jar
jetty-util-6.1.26.jar
jrobin-1.5.9.1.jar
org.springframework.web-3.1.0.RELEASE.jar
servlet-api-3.0.jar
cglib-nodep-2.2.2.jar

#2.web集成：
在被监控项目web.xml中加入如下代码 
<filter> 
<filter-name>monitoring</filter-name> 
<filter-class>net.bull.javamelody.MonitoringFilter</filter-class> 
</filter> 
<filter-mapping> 
<filter-name>monitoring</filter-name> 
<url-pattern>/*</url-pattern> 
</filter-mapping> 
<listener> 
<listener-class>net.bull.javamelody.SessionListener</listener-class> 
</listener>
将javamelody.jar,jrobin-1.5.9.1.jar复制到被监控项目的lib目录 

通过http://localhost:8080/monitoring访问