/**
 * Project: dianping_crawler
 * Package: com.bbdtek.util
 * File: Stringutil.java
 * Function: TODO
 * Version: 1.0
 * Date: 2011-4-10 上午12:36:21
 */
package com.manle.push.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xiongwei
 *
 */
public class Stringutil {
    
	public static String noneNullString(Object obj) {
		return (null==obj)?"":(""+obj);
	}
    public static boolean valid(String s) {
	return s != null && !"".equals(s.trim());
    }
    
    public static String escapeNull(String s) {
	return s == null ? "" : s;
    }
    public static String getThrowableStackTrace(Throwable t){
		if (t == null){
			return "";
		}
		String result = "";
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.flush();
		result = sw.getBuffer().toString();
		pw.close();
		return result;
	}

	
	public static String join(Object[] array, String joiner){
		
		String temp ="";
		if (array == null || array.length == 0){
			return "";
		}
		StringBuffer sb = new StringBuffer();
		int i = 0;
		for (Object obj: array){
			sb.append(obj == null ? "" : obj);
			if (i != array.length - 1) {
				sb.append(joiner);
			}
			i++;
		}
		
		try {
			temp = new String(sb.toString().getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return temp;
	}
	
	public static String join(Collection<?> col, String joiner){
		if (col == null || col.size() == 0){
			return "";
		}
		StringBuffer sb = new StringBuffer();
		int i = 0;
		for (Object obj: col){
			sb.append(obj == null ? "" : obj);
			if (i != col.size() - 1) {
				sb.append(joiner);
			}
			i++;
		}
		return sb.toString();
	}
	
	public static String changeEncoding(String str, String from, String to) throws UnsupportedEncodingException{
		return new String(str.getBytes(from), to);
	}
	
	public static String filterNull(String str){
		if (null == str){
			return "";
		}
		return str;
	}

	public static int parseInt(String str, int def){
		if (!valid(str)){
			return def;
		}
		return Integer.parseInt(str);
	}
	
	public static float parseFloat(String str, float def){
		if (!valid(str)){
			return def;
		}
		return Float.parseFloat(str);
	}
	
	public static double parseDouble(String str, double def){
		if (!valid(str)){
			return def;
		}
		return Double.parseDouble(str);
	}
	
	public static boolean parseBoolean(String str, boolean def){
		if (!"true".equalsIgnoreCase(str) || !"false".equalsIgnoreCase(str)){
			return def;
		}
		return Boolean.parseBoolean(str);
	}

	
	public static boolean startsWith(String str, String prefix) {
		Pattern p = Pattern.compile(prefix);
		Matcher m = p.matcher(str);
		if (m.find()){
			String pre = m.group();
			int offset = m.end();
			return offset == pre.length();
		}
		return false;
	}
	
	public static boolean endsWith(String str, String suffix) {
		Pattern p = Pattern.compile(suffix);
		Matcher m = p.matcher(str);
		int offset = 0;
		while (m.find()){
			offset = m.end();
		}
		return offset == str.length();
	}
	
	public static String getFirstMatch(String str, String regex) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(str);
		String temp = null;
		if (m.find()){
			temp = m.group();
		}
		return temp;
	}
	
	public static String getLastMatch(String str, String regex) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(str);
		String temp = null;
		while (m.find()){
			temp = m.group();
		}
		return temp;
	}
	
	
	public static long string2long(String sourceTime, String dataFormat) {
		long longTime = 0L;
		DateFormat f = new SimpleDateFormat(dataFormat);
		Date d = null;
		try {
			d = f.parse(sourceTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		longTime = d.getTime();
		return longTime;
	}
}
