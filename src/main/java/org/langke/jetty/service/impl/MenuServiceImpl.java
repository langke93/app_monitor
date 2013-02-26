package org.langke.jetty.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.langke.jetty.bean.Menu;
import org.langke.jetty.dao.IMenuDao;
import org.langke.jetty.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;

@Service("menuService")
public class MenuServiceImpl implements MenuService {

	private IMenuDao menuDao;
	@Autowired
	public void setMenuDao(IMenuDao menuDao) {
		this.menuDao = menuDao;
	}
	
	@Override
	public boolean add(Menu menu) {
		return menuDao.add(menu);
	}

	@Override
	public List<Map<String,Object>> query(Menu menu) {
		return menuDao.query(menu);
	}

	public List<Menu> getMenuTree() {
		List<Map<String,Object>> list = menuDao.query(null);
		List<Menu> menuList = new ArrayList<Menu>();
		if(list == null)
			return menuList;
		for(Map<String,Object> map:list){
			Menu node = TypeUtils.castToJavaBean(map, Menu.class,ParserConfig.getGlobalInstance());
			if(node.getF_id()==0){
				menuList.add(node);
			}else{
				for(Menu root:menuList){
					if(root.getId().equals(node.getF_id())){
						if(root.getChilden() == null)
							root.setChilden(new ArrayList<Menu>());
						root.getChilden().add(node);
						break;
					}
				}
			}
		}
		return menuList;
	}
	@Override
	public List<Menu> get(Menu menu) {
		List<Map<String,Object>> list = menuDao.get(menu);
		List<Menu> menuList = new ArrayList<Menu>();
		if(list == null)
			return menuList;
		for(Map<String,Object> map:list){
			Menu node = TypeUtils.castToJavaBean(map, Menu.class,ParserConfig.getGlobalInstance());
			menuList.add(node);
		}
		return menuList;
	}

	@Override
	public boolean update(Menu menu) {
		return menuDao.update(menu);
	}

	@Override
	public boolean del(Menu menu) {
		return menuDao.del(menu);
	}

}
