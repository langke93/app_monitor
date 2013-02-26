package org.langke.jetty.dao.impl;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.langke.jetty.bean.Menu;
import org.langke.jetty.common.TableUtil;
import org.langke.jetty.dao.IMenuDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * h2 手册http://h2database.com/html/grammar.html
 * @author langke
 * 2013.1.17
 *
 */
@Repository("menuDao")
public class MenuDao  extends JdbcDaoSupport implements IMenuDao {

	private static final Logger log = LoggerFactory.getLogger(MenuDao.class);
	DataSource ds;
	@Autowired
	public void setDs(DataSource ds) {
		super.setDataSource(ds);
		this.ds = ds;
	}

	public void create() {
		String batchSQL[] = generateCreateTableSql(TableUtil.T_MENU);
		int[] res = this.getJdbcTemplate().batchUpdate(batchSQL);//创建表
		//this.getJdbcTemplate().update("alter table t_menu add column `orders` int not null default 0");
		log.info("create {}",res );
	}

	private String[] generateCreateTableSql(String table_name){
		String batchSQL[] = { "CREATE TABLE IF NOT EXISTS `"+table_name+"` ( "+
				  "`id` int IDENTITY PRIMARY KEY, "+
				  "`f_id` int NOT NULL  DEFAULT 0 COMMENT 'parent id', "+
				  "`name` varchar(40) NOT NULL DEFAULT '', "+
				  "`url` varchar(255) NOT NULL  DEFAULT '', "+
				  "`orders` int NOT NULL DEFAULT 0, "+
				  "`status` tinyint NOT NULL  DEFAULT 1 COMMENT '0无效，1有效', "+
				  "`extend` varchar(40) NOT NULL DEFAULT '' COMMENT 'hostname'"+
				") ",
				};
		return batchSQL;
	}

	@Override
	public boolean add(Menu menu) {
		this.create();//如果表不存在则先建表
		String sql = "insert into "+TableUtil.T_MENU+" (f_id,name,url,status,orders,extend)values(?,?,?,?,?,?)";
		int res = this.getJdbcTemplate().update(sql,menu.getF_id(),menu.getName(),menu.getUrl(),1,menu.getOrders(),menu.getExtend());
		log.info("insert {}",res);
		return res>0;
	}

	@Override
	public List<Map<String,Object>> query(Menu menu) {
		String sql = "select id,f_id,(select b.name from t_menu b where b.id=a.f_id) f_name,name,url,orders,extend from t_menu a where status=1 order by f_id,orders";
		try{
			return this.getJdbcTemplate().queryForList(sql);
		}catch(Exception e){
			log.error("{}",e);
			return null;
		}finally{
			//log.info("{}",sql);
		}
	}

	public List<Map<String,Object>> queryFmenu(Menu menu) {
		try{ 
			String sql = "select id,f_id,name,url,orders,extend from "+TableUtil.T_MENU+" where status=1 ";
			return this.getJdbcTemplate().queryForList(sql);
		}catch(Exception e){
			log.error("{}",e);
			return null;
		} 
	}

	@Override
	public List<Map<String,Object>> get(Menu menu) {
		String sql = "select id,f_id,name,url,orders,extend from "+TableUtil.T_MENU+" where status=1 and id=?";
		return this.getJdbcTemplate().queryForList(sql, menu.getId());
	}

	@Override
	public boolean update(Menu menu) {
		String sql = "update "+TableUtil.T_MENU+" set name=? ,url=? ,f_id=?,orders=?,extend=?  where id=?";
		return this.getJdbcTemplate().update(sql ,menu.getName(),menu.getUrl(),menu.getF_id(),menu.getOrders(),menu.getExtend(),menu.getId()) > 0;
	}

	@Override
	public boolean del(Menu menu) {
		String sql = "update "+TableUtil.T_MENU+" set status=0 where id=?";
		return this.getJdbcTemplate().update(sql ,menu.getId()) > 0;
	}

}
