package org.langke.jetty.service;

import java.util.List;
import java.util.Map;

import org.langke.jetty.bean.Menu;

public interface MenuService {

	public boolean add(Menu menu);
	public List<Map<String,Object>> query(Menu menu);
	public List<Menu> getMenuTree();
	public List<Menu> get(Menu menu);
	public boolean update(Menu menu);
	public boolean del(Menu menu);
}
