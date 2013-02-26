package org.langke.jetty.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.langke.jetty.bean.Menu;
import org.langke.jetty.common.HttpClientUtil;
import org.langke.jetty.common.Share;
import org.langke.jetty.common.StrIntBag;
import org.langke.jetty.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/menu")
public class MenuController {
	@Autowired
	private MenuService menuService;
	
	@RequestMapping("addMenu.do")   
    protected ModelAndView addMenu(HttpServletRequest request, HttpServletResponse response, HttpSession session){   
		Menu menu = (Menu) Share.requestBean(request, Menu.class);
		menuService.add(menu);
		return menuList(request, response, session);
	}
	
	@RequestMapping("updateMenu.do")
	protected ModelAndView updateMenu(HttpServletRequest request,HttpServletResponse response,HttpSession session){
		Menu menu = (Menu)Share.requestBean(request, Menu.class);
		menuService.update(menu);
		return menuList(request, response, session);
	}
	
	@RequestMapping("delMenu.do")
	protected ModelAndView delMenu(HttpServletRequest request,HttpServletResponse response,HttpSession session){
		Menu menu = (Menu)Share.requestBean(request, Menu.class);
		menuService.del(menu);
		return menuList(request,response,session);
	}

	@RequestMapping("menuList.do")   
    protected ModelAndView menuList(HttpServletRequest request, HttpServletResponse response, HttpSession session){   
		//Menu menu = (Menu) Share.requestBean(request, Menu.class);
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("list", menuService.getMenuTree());
		return new ModelAndView("/menu/menuList.jsp", "res", map);
	}
	
	@RequestMapping("menuLeft.do")
    protected ModelAndView menuLeft(HttpServletRequest request, HttpServletResponse response, HttpSession session){   
		return new ModelAndView("/side.jsp", "list", menuService.getMenuTree());
	}
	
	@RequestMapping("url.do")
    protected ModelAndView url(HttpServletRequest request, HttpServletResponse response, HttpSession session){
		Menu menu = (Menu) Share.requestBean(request, Menu.class);
		List<Menu> list = menuService.get(menu);
		StrIntBag sb = new StrIntBag();
		if(list != null && list.size()>0){
			menu = list.get(0);
			sb = HttpClientUtil.executeGet(menu.getUrl(), menu.getExtend());
		}
		//Share.printWriter(sb._str, response);
		//return null;
		return new ModelAndView("/menu/url.jsp", "res", sb._str);
	}
	
}
