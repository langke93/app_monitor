1.javamelody集成Application
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

2.web集成：
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

通过http://ip:port/monitoring访问