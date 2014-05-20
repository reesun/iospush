/**
 * Project : ParserToolXD
 * FileName: Config.java
 * Created : 2011-10-24
 * Author  : xiongwei
 * Version : 1.0
 */
package com.manle.push.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.dbcp.BasicDataSource;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
	public static boolean isInit = false;
	private static Logger log = LoggerFactory.getLogger(Config.class);
	public static ExecutorService executorServer = null;
	private static int threadPoolSize = 10;
	public static List<PageParseConfig> listmap = new ArrayList<PageParseConfig>();
	public static Map<String, String> mapParams = new HashMap<String, String>();
	public static Map<String, String> mapDB = new HashMap<String, String>();
	public static int getThreadPoolSize(){
		return threadPoolSize;
	}
	public static void init(String configFile){
		if(!isInit){
			//"newParserConfig.xml"
			File file = new File(configFile);
			if(file.exists()&&file.canRead()){
				SAXReader reader = new SAXReader();
				reader.setEncoding("utf-8");
				Document doc = null;
				try {
					doc = reader.read(file);
				} catch (DocumentException e) {
					log.error(Stringutil.getThrowableStackTrace(e));
				}
				if(doc==null){
					log.error("newParserConfig.xml get null doc...");
					return;
				}
				Element root = doc.getRootElement();
				threadPoolSize = Integer.parseInt(root.element("threadpoolsize").getText());
				List dbs = root.elements("db");
				if(dbs!=null&&dbs.size()>0){
					for(Object objdb : dbs){
						Element db = (Element)objdb;
						if(db!=null){
							String dburl = db.attributeValue("url");
							String dbrootname = db.attributeValue("rootname");
							String dbpassword = db.attributeValue("password");
							String dbname = db.attributeValue("name");
							String id = db.attributeValue("id");
							mapDB.put(id, dbname);
							BasicDataSource bds = new BasicDataSource();
							bds.setDriverClassName("com.mysql.jdbc.Driver");
							bds.setUrl("jdbc:mysql://"+dburl+"/"+dbname+"?useUnicode=true&characterEncoding=utf8&autoReconnect=true");
							bds.setUsername(dbrootname);
							bds.setPassword(dbpassword);
							bds.setMaxWait(-1);
							bds.setValidationQuery("select 1");
							bds.setTestWhileIdle(true);
							bds.setTimeBetweenEvictionRunsMillis(3600000);
							bds.setMinEvictableIdleTimeMillis(1800000);
							bds.setTestOnBorrow(true);
							ConnectionFactory.init(id, bds);
							List tables = db.elements("table");
							if(tables!=null&&tables.size()>0){
								for(Object obj : tables){
									boolean isTableExist = false;
									Element table = (Element)obj;
									String tablename = table.attributeValue("name");
									Connection conn = null;
									PreparedStatement ps = null;
									ResultSet rs = null;
									try {
										conn = ConnectionFactory.getConnection(id);
										ps = conn.prepareStatement("show tables");
										rs = ps.executeQuery();
										log.info(rs.toString());
										while(rs.next()){
											if(tablename.equals(rs.getString(1))){
												isTableExist = true;
												break;
											}
										}
										if(!isTableExist){
											//create db table...
											log.info("start create new table..");
											List fields = table.elements("field");
											String cSQL = "CREATE TABLE `"+tablename+"` (`id` int(255) unsigned NOT NULL auto_increment";
											for(Object obj2 : fields){
												Element field = (Element)obj2;
												String fname = field.attributeValue("name");
												String ftype = field.attributeValue("fieldtype");
												cSQL = cSQL+", `"+fname+"` "+ftype;
												String fdefault = field.attributeValue("default");
												if(fdefault!=null){
													cSQL = cSQL+" default "+fdefault;
												}
												
											}
											cSQL = cSQL + ", PRIMARY KEY  (`id`)) ENGINE=MyISAM  DEFAULT CHARSET=utf8;";
											log.info(cSQL);
											ps = conn.prepareStatement(cSQL);
											ps.execute();
										}
									} catch (SQLException e) {
										log.error(Stringutil.getThrowableStackTrace(e));
									} finally{
										DBUtil.tryRelease(conn);
										DBUtil.tryRelease(ps);
										DBUtil.tryRelease(rs);
									}
								}//for tables
							}
							
						}
					}//for dbs
				}
				List listparams = root.elements("params");
				if(listparams!=null&&listparams.size()>0){
					for(Object paramobj : listparams){
						Element param = (Element)paramobj;
						if(param!=null){
							String name = param.attributeValue("name");
							String value = param.attributeValue("value");
							mapParams.put(name, value);
						}
					}
				}
				//���������������������������
				List pl = root.elements("page");
				configParserParams(pl, listmap);
			} else{
				log.error("file newParserConfig is not exist or cannot be read...");
			}
			executorServer = Executors.newFixedThreadPool(threadPoolSize);
			isInit = true;
		}
		
	}
	/**
	 * @param pl
	 * @param listmap2 
	 */
	private static void configParserParams(List pl, List<PageParseConfig> listmap2) {
		if(pl!=null&&pl.size()>0){
			for(Object obj : pl){
				PageParseConfig plObject = new PageParseConfig();
				Element pageList = (Element)obj;
				
				List listparam = pageList.attributes();
				for(Object objpar : listparam){
					Attribute att = (Attribute)objpar;
					plObject.att.put(att.getName(), att.getValue());
				}
				
				plObject.name = pageList.attributeValue("name");
				if(pageList.attribute("url")!=null){
					plObject.url = pageList.attributeValue("url");
				}
				if(pageList.attribute("urlfromdb")!=null){
					plObject.urlfromdb = pageList.attributeValue("urlfromdb");
				}
				if(pageList.attribute("urlhead")!=null){
					plObject.urlhead = pageList.attributeValue("urlhead");
				}
				List listInfo = pageList.elements("infor");
				parseConfigInfor(listInfo, plObject.infors);
				listmap2.add(plObject);
			}
		}
	}
	/**
	 * @param listInfo
	 * @param infors
	 */
	private static void parseConfigInfor(List listInfo, List<ConfigInfor> infors) {
		if(listInfo!=null&&listInfo.size()>0){
			for(Object objinf : listInfo){
				ConfigInfor ci = new ConfigInfor();
				Element info = (Element)objinf;
				List listparam = info.attributes();
				for(Object objpar : listparam){
					Attribute att = (Attribute)objpar;
					ci.att.put(att.getName(), att.getValue());
				}
				List innerInfor = info.elements("infor");
				parseConfigInfor(innerInfor, ci.listInfor);
				infors.add(ci);
			}
		}
	}
	
}