package org.langke.jetty.service.impl;

import org.langke.jetty.dao.IAppMonitorDao;
import org.langke.jetty.service.AppMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;

@Service("appMonitorService")
public class AppMonitorServiceImpl implements AppMonitorService{

	private static final Logger logger = LoggerFactory.getLogger(AppMonitorServiceImpl.class);
	@Autowired
	IAppMonitorDao appMonitorDao;
	@Override
	public void create() {
		logger.info("create ... ");
		appMonitorDao.create();
	}

	public Object insert(JSONArray jsoarr){
		return appMonitorDao.insert(jsoarr);
	}
}
