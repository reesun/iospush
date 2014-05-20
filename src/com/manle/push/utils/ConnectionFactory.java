package com.manle.push.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;

public class ConnectionFactory {
	private static Map<String, BasicDataSource> dss = new HashMap<String, BasicDataSource>();
	private ConnectionFactory(){}
	
	public static void init(String id, BasicDataSource bds){
		if (bds == null){
			return;
		}
		dss.put(id, bds);
	}
	
	public static synchronized Connection getConnection(String id) throws SQLException{
		BasicDataSource ds = dss.get(id);
		if (ds == null){
			return null;
		}
		return ds.getConnection();
	}
	
	public static synchronized void destroy(){
		if (dss != null){
			for (Map.Entry<String, BasicDataSource> entry: dss.entrySet()){
				BasicDataSource ds = entry.getValue();
				try {
					ds.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
