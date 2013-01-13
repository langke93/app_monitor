package org.langke.jetty.service;

import com.alibaba.fastjson.JSONArray;

public interface AppMonitorService {

	public void create();
	public Object insert(JSONArray jsoarr);
}
