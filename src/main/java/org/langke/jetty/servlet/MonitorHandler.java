package org.langke.jetty.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.langke.jetty.common.CostTime;
import org.langke.jetty.common.DateUtil;
import org.langke.jetty.common.Dbop;
import org.langke.jetty.common.RequestUtil;
import org.langke.jetty.common.TableUtil;
import org.langke.jetty.common.chart.ChartUtil;
import org.langke.jetty.resp.Response;
import org.langke.jetty.server.SpringApplicationContext;
import org.langke.jetty.service.AppMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * Servlet implementation class MonitorHandler
 */

public class MonitorHandler extends HttpServlet {
	Statement st = null;
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(MonitorHandler.class);

	AppMonitorService appMonitorService = (AppMonitorService) SpringApplicationContext.getInstance().getService("appMonitorService");
	 
	String DBType;

	/**
     * @see HttpServlet#HttpServlet()
     */
    public MonitorHandler() {
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
		String method = request.getPathInfo();
		Response resp = new Response();
		Object result = null;
		if(method.equalsIgnoreCase("/favicon.ico"))
			return;
		//log.debug("request:{}",request);
		if(method.equalsIgnoreCase("/_report"))
			report(request, response);
		else if(method.equalsIgnoreCase("/_add"))
			result = _add(request, response) ;
		else
			result = "not fund method:"+method;
		resp.setResult(result);
		resp.setCostTime(cost.cost());
		log.info("{} cost:{}", method,resp.getCostTime());
		response.getWriter().print(JSONObject.toJSONString(resp, SerializerFeature.PrettyFormat));
	}
	
	private Object _add(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    String body = RequestUtil.getRequestString(request);
	    JSONArray jsoarr = JSONObject.parseArray(body);
	    return appMonitorService.insert(jsoarr);
	}
	private void report(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CostTime cost = new CostTime();
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		PrintWriter pw = response.getWriter();
		String basePath = request.getContextPath()+"/";
		String barSQL = null;
		String startDay = request.getParameter("startDay");
		String endDay = request.getParameter("endDay");
		String auto_refresh = request.getParameter("auto_refresh");
		String server_ip = request.getParameter("server_ip");
		String series = request.getParameter("series");
		String table_name = TableUtil.table_name;
		if(StringUtils.isNotEmpty(startDay)){
			table_name = TableUtil.table_name_pre+DateUtil.format(DateUtil.parseStringToDateTime(startDay),"yyyy_MM_dd");
		}else{
			Date date = new Date();
			startDay = DateUtil.getDatetimeStr(DateUtils.addHours(date, -1).getTime());
		}
		if(!StringUtils.isNotEmpty(endDay)){ 
		}
		if(DBType == null){
			try {
				DBType = new Dbop().GetConnection().getMetaData().getDatabaseProductName();
			} catch (SQLException e) {
				log.error("",e);
			}
		}
		List<Map<String, Object>> list = null;
		Map<String,Object> map = null;     
		Map<String,Object> map2 = null;   	
		String analysisfilename = "";
	   	String analysisgraphURL = "";
		int width=660;
	  	int height=380;
		String where="";
		if(StringUtils.isNotEmpty(startDay) && StringUtils.isNotEmpty(endDay))
			where += " and add_time between '"+startDay+"' and '"+endDay+"' ";
		else if(StringUtils.isNotEmpty(startDay))
			where += " and add_time >= '"+startDay+"' ";
		else if(StringUtils.isNotEmpty(endDay))
			where += " and add_time <= '"+endDay+"' ";
		if(StringUtils.isNotEmpty(series)){
			where += " and app_name = '"+series+"' ";
		}
		List<Map<String,Object>> ipList = Dbop.getQueryList("select server_ip from "+table_name+" where 1=1 "+where+" group by server_ip");
		if(StringUtils.isNotEmpty(server_ip))
			where += " and server_ip = '"+server_ip+"' ";
		String htmlstr = "";
	  	String script = "<script language=\"javascript\" src=\""+basePath+"js/DatePicker/WdatePicker.js\"></script>";
	  	script +="<script>onload = function() {window.setInterval(exec_func, 60000);} ; ";
	    script +=" function exec_func(){if(document.getElementById(\"auto_refresh\").checked==true){document.form.submit();}};  </script>\n";
	    htmlstr +=script;

	  	htmlstr+=" <form name=form>\n";
	  	if(StringUtils.isNotEmpty(series)){
	  		htmlstr += "<input type=hidden name=series value=\""+series+"\"/>";
		}
	  	htmlstr+="	server:<select name=\"server_ip\" onchange=\"document.form.submit();\">\n";
	  	htmlstr+="		<option value=\"\">all</option>\n";
	  	for(Map<String,Object> ipMap:ipList){
		  	htmlstr+="		<option value=\""+ipMap.get("SERVER_IP")+"\" "+(ipMap.get("SERVER_IP").equals(StringUtils.trimToNull(server_ip))?"selected":"")+">"+ipMap.get("SERVER_IP")+"</option>";
	  	}
	  	htmlstr+="		</select>\n";
	  	htmlstr+="		from<input name=\"startDay\" id=\"startDay\" type=\"text\" value=\""+StringUtils.trimToEmpty(startDay)+"\" class=\"Wdate\" onclick=\"WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss'})\"/>&nbsp;To&nbsp;\n";
	  	htmlstr+="			<input name=\"endDay\" id=\"endDay\" type=\"text\" value=\""+StringUtils.trimToEmpty(endDay)+"\" class=\"Wdate\" onclick=\"WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss'})\"/>\n";
	  	htmlstr+=" auto load 1/min<input type=\"checkbox\" name=auto_refresh id=auto_refresh value=1 "+(auto_refresh!=null?"checked":"")+">";
	  	htmlstr+="			<input type=button value=query onclick=\"document.form.submit();\">";
	  	htmlstr+="</form>";
	  	out.println(htmlstr);
		out.println("<table>");
		barSQL = "select type,status from "+table_name+" where 1=1 "+where+" group by status order by status desc";
		String mapUrl = null;
		cost.start();
		list = Dbop.getQueryList(barSQL);
		log.debug("sql:{} time:{}", barSQL,cost.cost());
		for(int i=0;i<list.size();i+=2){
			map =  list.get(i);
			if(list.size()>i+1)
				map2 = list.get(i+1);
			mapUrl = request.getContextPath()+"/monitor/_report";
			out.println("<tr><td>");
			//thread count
			if(DBType.equalsIgnoreCase("H2")){
				barSQL = "select app_name,PARSEDATETIME(FORMATDATETIME(add_time,'yyyy-MM-d H:m'),'yyyy-MM-d H:m') add_time,sum(val) from "+table_name+" where 1=1 and status='"+map.get("STATUS")+"'  "+where+" group by FORMATDATETIME(add_time,'yyyy-MM-d H:m'),add_time,app_name";//h2搞什么？函数不统一
			}else{
				barSQL = "select app_name,str_to_date(date_format(add_time,'%Y-%m-%d %H:%i'),'%Y-%m-%d %H:%i'),sum(val) from "+table_name+" where 1=1 and status='"+map.get("STATUS")+"'  "+where+" group by date_format(add_time,'%Y-%m-%d %H:%i'),app_name";
			}
				
			try { 
		   		cost.start();
				analysisfilename = ChartUtil.generateXYCurveLineChart(barSQL,  pw,map.get("TYPE")+"-"+ map.get("STATUS"),null,null,true, mapUrl, true, width, height, "frame_"+i);
				log.debug("sql:{} time:{}", barSQL,cost.cost());
			} catch (Exception e) {
				e.printStackTrace();
			}
		 	analysisgraphURL = request.getContextPath() + "/servlet/DisplayChart?filename=" + analysisfilename;
		 	out.println("<img src="+ analysisgraphURL +" border=0 usemap=\"#"+analysisfilename+"\">");
			out.println("	</td>");
			
			/** right td **/ 
			if(list.size()>i+1)
				;
			else 
				continue;
			out.println("	<td>");
			
			if(DBType.equalsIgnoreCase("H2")){
				barSQL = "select app_name,PARSEDATETIME(FORMATDATETIME(add_time,'yyyy-MM-d H:m') ,'yyyy-MM-d H:m') add_time,sum(val) from "+table_name+" where 1=1 and status='"+map2.get("STATUS")+"'  "+where+" group by FORMATDATETIME(add_time,'yyyy-MM-d H:m'),app_name";//h2写法
			}else{
				barSQL = "select app_name,str_to_date(date_format(add_time,'%Y-%m-%d %H:%i'),'%Y-%m-%d %H:%i'),sum(val) from "+table_name+" where 1=1 and status='"+map2.get("status")+"'  "+where+" group by date_format(add_time,'%Y-%m-%d %H:%i'),app_name";
			}
			try { 
		   		cost.start();
				analysisfilename = ChartUtil.generateXYCurveLineChart(barSQL,  pw,map2.get("TYPE")+"-"+ map2.get("STATUS"),null,null,true, mapUrl, true, width, height,"frame_"+i);
				log.debug("sql:{} time:{}", barSQL,cost.cost());
			} catch (Exception e) {
				e.printStackTrace();
			}
		 	analysisgraphURL = request.getContextPath() + "/servlet/DisplayChart?filename=" + analysisfilename;
		 	out.println("<img src="+ analysisgraphURL +" border=0 usemap=\"#"+analysisfilename+"\">");
			out.println("	</td></tr>");
		}
		out.print("</table>");
	}
}
