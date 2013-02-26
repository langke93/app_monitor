package org.langke.jetty.dao;

import java.util.List;
import java.util.Map;

import org.langke.jetty.bean.Menu;

public interface IMenuDao {

	public boolean add(Menu menu);
	public List<Map<String,Object>> query(Menu menu);
	public List<Map<String,Object>> queryFmenu(Menu menu);
	public List<Map<String,Object>> get(Menu menu);
	public boolean update(Menu menu);
	public boolean del(Menu menu);
}
