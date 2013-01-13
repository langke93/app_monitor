package org.langke.jetty.dao.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.langke.jetty.common.DateUtil;
import org.langke.jetty.common.TableUtil;
import org.langke.jetty.dao.IAppMonitorDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * h2 手册http://h2database.com/html/grammar.html
 * @author langke
 * 2013.1.12
 *
 */
public class AppMonitorDao extends JdbcDaoSupport implements IAppMonitorDao{

	private static final Logger log = LoggerFactory.getLogger(AppMonitorDao.class);

	@Override
	public void create() {
		String batchSQL[] = generateCreateTableSql(TableUtil.table_name);
		this.getJdbcTemplate().batchUpdate(batchSQL);//创建当天的表
		
		Date tomorrow = DateUtils.addDays(Calendar.getInstance().getTime(), 1);
		batchSQL = generateCreateTableSql(TableUtil.table_name_pre+DateUtil.format(tomorrow, "yyyy_MM_dd"));
		this.getJdbcTemplate().batchUpdate(batchSQL);//创建次日的表

		String sql = "select table_name from information_schema.`TABLES` where  TABLE_NAME like 'T_API_MONITOR_%'";
		List<Map<String,Object>> list = this.getJdbcTemplate().queryForList(sql);
		log.info("tables :{}",list);
	}
	
	public void drop(){
		String sql = "select * from information_schema.`TABLES` where  TABLE_NAME like 'T_API_MONITOR_%'";
		List<Map<String,Object>> list = this.getJdbcTemplate().queryForList(sql);
		for(Map<String,Object> map:list){
			log.info("drop table :{}",map);
			this.getJdbcTemplate().update("DROP TABLE IF EXISTS `"+map.get("TABLE_NAME")+"`");
		}
	}

	private String[] generateCreateTableSql(String table_name){
		String batchSQL[] = { "CREATE TABLE IF NOT EXISTS `"+table_name+"` ( "+
				  "`group_name` varchar(40) DEFAULT NULL, "+
				  "`server_ip` varchar(40) DEFAULT NULL, "+
				  "`app_name` varchar(40) DEFAULT NULL, "+
				  "`type` varchar(40) DEFAULT NULL COMMENT 'cpu,mem,thread,networkconn', "+
				  "`status` varchar(40) DEFAULT NULL COMMENT 'RUNNABLE,WAIT', "+
				  "`val` int(12) DEFAULT NULL, "+
				  "`add_time` datetime DEFAULT NULL, "+
				  "`mark` varchar(40) DEFAULT NULL "+
				") ",
				"CREATE INDEX IF NOT EXISTS IDX_T_API_MONITOR_ADD_TIME ON `"+table_name+"`(`add_time`)",
				"CREATE INDEX IF NOT EXISTS IDX_T_API_MONITOR_STATUS ON `"+table_name+"`(`status`)"
				};
		return batchSQL;
	}
	
	public Object insert(JSONArray jsoarr){
	    JSONObject jso = null;
	    Object result;
		String sql = "insert into "+TableUtil.table_name+"(group_name,server_ip,app_name,type,status,val,add_time,mark) " +
		    		"values(?,?,?,?,?,?,?,?)";
	    //log.debug("sql:{} columnNames:{}", sql,jsoarr); 
		try {
			List<Object[]> batchArgs = new ArrayList<Object[]>();
			for(int index= 0;index<jsoarr.size();index++){
				jso = jsoarr.getJSONObject(index);
			    String group_name = "defaultGroup";
			    if(jso.containsKey("group_name"))
			    	group_name = jso.getString("group_name");
			    String server_ip = jso.getString("server_ip");
			    String app_name = jso.getString("app_name");
			    String type = jso.getString("type");
			    String status = jso.getString("status");
			    String add_time = DateUtil.getCurrentDateStr();
			    String val = jso.getInteger("val").toString();
			    String mark = jso.getString("mark");
				String columnNames[] = {group_name,server_ip,app_name,type,status,val,add_time,mark};
				batchArgs.add(columnNames);
			}
			int[] res = this.getJdbcTemplate().batchUpdate(sql, batchArgs);
			//int[] res = ps.executeBatch();
			return res;
		} catch (Exception e) {
			log.error("",e);
			result = e.getMessage(); 
		} 
		return result;
	}
}
