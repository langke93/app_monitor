package org.langke.jetty.resp;

import com.alibaba.fastjson.annotation.JSONField;

public class Response{
	private Integer statusCode=200;
	private String msg;
	@JSONField(name = "result")
	private Object result;	
	private long costTime;
	public Integer getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	public long getCostTime() {
		return costTime;
	}
	public void setCostTime(long costTime) {
		this.costTime = costTime;
	}
	
}
