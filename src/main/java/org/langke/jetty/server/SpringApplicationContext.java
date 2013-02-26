package org.langke.jetty.server;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
 

public class SpringApplicationContext {
	
	private static ApplicationContext ctx;
	private static SpringApplicationContext instance=new SpringApplicationContext();
	
	public  static SpringApplicationContext getInstance(){
		return instance;
	}
	
	private SpringApplicationContext() {
		initCtx();
	}
	
	private void initCtx(){
		if(ctx == null) {
			String[] locations = {"classpath:applicationContext.xml"};
			ctx = new ClassPathXmlApplicationContext(locations);
	     }  
	}
	
	public Object  getService(String serviceName){
		return ctx.getBean(serviceName);
	}
	
	public ApplicationContext getApplicationContext(){
		return ctx;
	}
 
}
