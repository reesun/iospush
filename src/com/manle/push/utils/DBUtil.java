/**
 * Project: dianping_crawler
 * Package: com.bbdtek.util
 * File: DBUtil.java
 * Function: TODO
 * Version: 1.0
 * Date: 2011-4-10 上午11:16:52
 */
package com.manle.push.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author xiongwei
 *
 */
public class DBUtil {
	private static final Logger log = LoggerFactory.getLogger(DBUtil.class);

	public static List<Map<String, Object>> sqlExec(String sql, String connfac){
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			conn = ConnectionFactory.getConnection(connfac);
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()){
				ResultSetMetaData rsmd = rs.getMetaData();
				int cc = rsmd.getColumnCount();
				Map<String, Object> map = new HashMap<String, Object>();
				for(int i=1; i<=cc; i++){
					String coluName = rsmd.getColumnName(i);
					Object value = rs.getObject(coluName);
					if(null==value){
						value = "";
					}
					map.put(coluName, value);
				}
				
				ret.add(map);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log.error(Stringutil.getThrowableStackTrace(e));
		} finally{
			tryRelease(rs);
			tryRelease(ps);
			tryRelease(conn);
		}
		
		
		
		return ret;
	}
	
	public static boolean exec(String sql, String connfac) {
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		Connection conn = null;
		PreparedStatement ps = null;
		boolean rs = false;
		
		try {
			conn = ConnectionFactory.getConnection(connfac);
			ps = conn.prepareStatement(sql);
			rs = ps.execute();
			
		} catch (SQLException e) {
			e.printStackTrace();
			log.error(Stringutil.getThrowableStackTrace(e));
		} finally{
			tryRelease(ps);
			tryRelease(conn);
		}
		
		return rs;
	}
	
	/**
	 * @param lm
	 * @param string
	 * @param string2
	 */
	public static void updateDB(List<Map<String, Object>> listmap, String table,
			String connfac) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.getConnection(connfac);
			for(Map<String, Object> map : listmap){
				String sql = "UPDATE `"+table+"` SET ";
				int id = Integer.parseInt(""+map.get("id"));
				List<Object> lstr = new ArrayList<Object>();
				String setstr = "";
				for(String key : map.keySet()){
					if(key.equals("id")){
						continue;
					}
					setstr += "`"+key+"` = ?, ";
					lstr.add(map.get(key));
				}
				if(setstr.endsWith(", ")){
					setstr = setstr.substring(0, setstr.length()-2);
				}
				sql += setstr + " WHERE `id` = ?";
				System.out.println(sql);
				ps = conn.prepareStatement(sql);
				int i = 1;
				for(Object obj : lstr){
					ps.setObject(i, obj);
					i++;
				}
				ps.setObject(i, id);
				ps.executeUpdate();
				ps.close();
			}
			
		} catch (SQLException e) {
			log.error(Stringutil.getThrowableStackTrace(e));
		} finally{
			DBUtil.tryRelease(ps);
			DBUtil.tryRelease(conn);
		}
		
		
	}
	/**
	 * @param listmap
	 * @param tablename
	 * @param connfactoryid 
	 */
	public static void saveDB(List<Map<String, Object>> listmap, String table, String connfac) {
		
		Map<String, Object> map = listmap.get(0);
		Object[] titlearr = map.keySet().toArray();
		String titles = "";
		String qstr = "";
		for(int i=0; i<titlearr.length; i++){
			
			titles += ("`"+titlearr[i]+"`, ");
			qstr += "?, ";
		}
		titles = titles.substring(0, titles.length()-2);
		qstr = qstr.substring(0, qstr.length()-2);
		
		
		Connection conn = null;
		PreparedStatement ps = null;
//		int errid = 0;
		String sqlc = "INSERT INTO `"+table+"` ("+titles+") VALUES ("+qstr+")";
//		System.out.println("aaa: "+sqlc);
		try {
			conn = ConnectionFactory.getConnection(connfac);
			ps = conn.prepareStatement(sqlc);
			for(Map<String, Object> tm : listmap){
				/*errid = Integer.parseInt(""+tm.get("solrid"));
				if(4117 == errid){
					continue;
				}*/
				for(int i=0; i<titlearr.length; i++){
					ps.setObject(i+1, tm.get(titlearr[i]));
				}
//				log.info(ps.toString());
//				log.info("1");
				ps.executeUpdate();
			}
		} catch (SQLException e) {
//			System.out.println("eeeee");
			log.error(Stringutil.getThrowableStackTrace(e));
//			log.info(""+errid);
		} finally{
			DBUtil.tryRelease(ps);
			DBUtil.tryRelease(conn);
		}	
		
	}
	/**
	 * @param listmap
	 * @param tablename
	 * @param connfactoryid 
	 */
	public static void saveDB2(List<Map<String, Object>> listmap, String table, String connfac) {
		
		/*Map<String, Object> map = listmap.get(0);
		Object[] titlearr = map.keySet().toArray();
		String titles = "";
		String qstr = "";
		for(int i=0; i<titlearr.length; i++){
			titles += ("`"+titlearr[i]+"`, ");
			qstr += "?, ";
		}
		titles = titles.substring(0, titles.length()-2);
		qstr = qstr.substring(0, qstr.length()-2);
		*/
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
//		int errid = 0;
		String sqlcol = "SELECT * FROM `"+table+"` LIMIT 0,1";
		
		try {
			conn = ConnectionFactory.getConnection(connfac);
			ps = conn.prepareStatement(sqlcol);
			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int cc = rsmd.getColumnCount();
			String[] titlearr = new String[cc];
			for(int i=1; i<=cc; i++){
				String colName = rsmd.getColumnName(i);
				titlearr[i-1] = colName;
			}
			String titles = "";
			String qstr = "";
			for(int i=0; i<titlearr.length; i++){
				titles += ("`"+titlearr[i]+"`, ");
				qstr += "?, ";
			}
			titles = titles.substring(0, titles.length()-2);
			qstr = qstr.substring(0, qstr.length()-2);
			rs.close();
			ps.close();
			
			
			
			String sqlc = "INSERT INTO `"+table+"` ("+titles+") VALUES ("+qstr+")";
			ps = conn.prepareStatement(sqlc);
			for(Map<String, Object> tm : listmap){
				/*errid = Integer.parseInt(""+tm.get("solrid"));
				if(4117 == errid){
					continue;
				}*/
				for(int i=0; i<titlearr.length; i++){
					ps.setObject(i+1, tm.get(titlearr[i]));
				}
//				log.info(ps.toString());
//				log.info("1");
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			
			log.error(Stringutil.getThrowableStackTrace(e));
//			log.info(""+errid);
		} finally{
			tryRelease(rs);
			DBUtil.tryRelease(ps);
			DBUtil.tryRelease(conn);
		}	
		
	}
    
    
    public static void tryRelease(Connection conn) {
	if(conn != null) {
	    try {
		conn.close();
	    } catch (SQLException e) {
	    }
	    conn = null;
	}
    }
    
    public static void tryRelease(Statement ps) {
	if(ps != null) {
	    try {
		ps.close();
	    } catch (SQLException e) {
	    }
	    ps = null;
	}
    }
    
    public static void tryRelease(ResultSet rs) {
	if(rs != null) {
	    try {
		rs.close();
	    } catch (SQLException e) {
	    }
	    rs = null;
	}
    }



	
}
