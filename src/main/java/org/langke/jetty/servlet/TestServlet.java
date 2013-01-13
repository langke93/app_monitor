package org.langke.jetty.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.langke.jetty.common.CostTime;
import org.langke.jetty.resp.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class TestServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6491268125454178487L;
	private static final Logger log = LoggerFactory.getLogger(TestServlet.class);
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TestServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
    	super.init(); 
    }
    @Override
    public void destroy() {
    	super.destroy(); 
    }
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("utf-8");
		response.setContentType("application/json");
		CostTime cost = new CostTime();
		cost.start();
		String method = request.getRequestURI();
		Response resp = new Response();
		Object result = request.getParameterMap();
		log.debug("request:{} {}",method,request);
		resp.setResult(result);
		resp.setCostTime(cost.cost());
		response.getWriter().print(JSONObject.toJSONString(resp, SerializerFeature.PrettyFormat));
		//	log.info("{} {}", method,cost.cost());
	}
	
}
