package org.langke.jetty.dao;

import com.alibaba.fastjson.JSONArray;

public interface IAppMonitorDao {

	public void create();
	public Object insert(JSONArray jsoarr);
}
