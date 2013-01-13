package  org.langke.jetty.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /***
  * 数据源用于报表
  * @author lee
  *
  */
public class Dbop {

    Object obj ;
    public int ErrorCode=0;
    public String ErrorMsg;
    private static DataSource ds;

	private static final Logger log = LoggerFactory.getLogger(Dbop.class);
	private DataSource createDataSource(){
		return createDataSource("dataSource");
	}
    private DataSource createDataSource(String dsname){
    	//获取连接池对象
		String[] locations = {"classpath:spring-context.xml"};
		ApplicationContext ctx = new ClassPathXmlApplicationContext(locations);
        return (DataSource) ctx.getBean(dsname);
    }
    
    public Dbop() {
      try{
    	if(ds == null)
    		ds = createDataSource();
        }catch(Exception ex){
         ErrorCode=-1;
         ErrorMsg=ex.getMessage();
         log.error("Dbop--获取连接池对象异常:"+ErrorMsg,ex);
      }
    }
    
    public Dbop(String dsname) {
        try{
        	if(ds == null)
        		ds = createDataSource(dsname);
            }catch(Exception ex){
             ErrorCode=-1;
             ErrorMsg=ex.getMessage();
             log.error("Dbop--获取连接池对象异常:"+ErrorMsg,ex);
          }
    }

    public  Connection  GetConnection(){
      Connection conn;
      try{
		conn=ds.getConnection();
		conn.setAutoCommit(false);
		return conn;
      }catch (SQLException ex){
    	  log.error("",ex);
        try{
          conn=ds.getConnection();
          conn.setAutoCommit(false);
        }catch(SQLException ex2){   
          ex2.printStackTrace();
          ErrorCode = ex.getErrorCode();
          ErrorMsg = ex.getMessage();
          return null;
        }
        return conn;
      }
    }
    
    
  public int CloseConnection(Connection conn){
    try {
      if (conn != null){
        conn.close();
        conn = null;
      }
      CloseContext();
      return 0;
    }
    catch (SQLException ex) {
		log.error("",ex);
      ErrorMsg = "关闭连接错误" + ex.getMessage();
      ErrorCode = ex.getErrorCode();
      return -1;
    }
  }

  public int CloseContext(){
    try {
      return(0);
    }catch (Exception ex) {
		log.error("",ex);
      return(-1);
    }
  }


	/**
	 * 实现修改
	 * @param sql
	 * @return
	 */
	public static boolean executeUpdate(String sql) {
        Dbop dbop = new Dbop();
		Connection connection = dbop.GetConnection();
		Statement st = null;
		try {
			st = connection.createStatement();
			int i = st.executeUpdate(sql);
			if (i > 0) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			log.error("",e);
		} finally {
			if(st!=null)
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			dbop.CloseConnection(connection);
		}
		return false;
	}
	
	/**
	 * 实现查询
	 * @param sql
	 * @return
	 */
	public static List<Map<String,Object>>  getQueryList(String sql) {
        Dbop dbop = new Dbop();
		Connection connection = dbop.GetConnection();
		List<Map<String,Object>> list = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			st = connection.createStatement();
			rs = st.executeQuery(sql);
			list = new ArrayList<Map<String,Object>>();
			while(rs.next()){
				int cols= rs.getMetaData().getColumnCount();
				Map<String,Object> map = new HashMap<String,Object>();
				for(int i=1;i<=cols;i++){
					//System.out.print(rs.getMetaData().getColumnName(i)+":"+rs.getString(i)+"    ");
					map.put(rs.getMetaData().getColumnName(i),rs.getString(i));
				}
				list.add(map);
			}
		} catch (SQLException e) {
			log.error("",e);
		}finally{
			if(st!=null)
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			dbop.CloseConnection(connection);
		}
		return list;
	}
}
