package org.langke.jetty.server;

import org.langke.jetty.service.AppMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@Controller 
public class Job {

	@Autowired
	AppMonitorService appMonitorService;
 
	/**
	 * 定时everday执行一次
	 * */
	@Scheduled(fixedRate=1*1000*60*60*24)
	public void createDay(){
		appMonitorService.create();
	}
	
}
