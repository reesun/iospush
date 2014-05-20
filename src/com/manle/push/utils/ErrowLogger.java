/**
 * Project : ParserToolXD
 * FileName: ErrowLogger.java
 * Created : 2011-12-19
 * Author  : xiongwei
 * Version : 1.0
 */
package com.manle.push.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用途：TODO
 * 
 * @author bbdtek
 */
public class ErrowLogger {
	private static Logger log = LoggerFactory.getLogger(ErrowLogger.class);

	/**
	 * @param clss
	 * @param string
	 * @param rootUrl
	 * @param rootUrl2
	 */
	public static void add(String cfg, String clss, String inf, String tag) {
		Config.init(cfg);
		Connection conn = null;
		PreparedStatement ps = null;
		String sql = "INSERT INTO errors (`clss`, `infor`, `tag`, `time`) VALUES (?, ?, ?, ?)";
		try {
			conn = ConnectionFactory.getConnection("39");
			ps = conn.prepareStatement(sql);
			ps.setString(1, clss);
			ps.setString(2, inf);
			ps.setString(3, tag);
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			ps.setString(4,
					formatter.format(new Date(System.currentTimeMillis())));
			ps.executeUpdate();
		} catch (SQLException e) {
			log.error(Stringutil.getThrowableStackTrace(e));
		} finally {
			DBUtil.tryRelease(ps);
			DBUtil.tryRelease(conn);
		}

	}

}
