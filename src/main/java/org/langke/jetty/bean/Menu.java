package org.langke.jetty.bean;

import java.util.List;

public class Menu {

	private Integer id;
	private Integer f_id;
	private String name;
	private String url;
	private byte status;
	private String extend;
	private Integer orders;
	private String f_name;
	private List<Menu> childen ;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getF_id() {
		return f_id;
	}
	public void setF_id(Integer f_id) {
		this.f_id = f_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public byte getStatus() {
		return status;
	}
	public void setStatus(byte status) {
		this.status = status;
	}
	public String getExtend() {
		return extend;
	}
	public void setExtend(String extend) {
		this.extend = extend;
	}
	public Integer getOrders() {
		return orders;
	}
	public void setOrders(Integer orders) {
		this.orders = orders;
	}
	public String getF_name() {
		return f_name;
	}
	public void setF_name(String f_name) {
		this.f_name = f_name;
	}
	public List<Menu> getChilden() {
		return childen;
	}
	public void setChilden(List<Menu> childen) {
		this.childen = childen;
	}
	
}
