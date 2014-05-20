/**
 * Project : ParserToolXD
 * FileName: PageInfo.java
 * Created : 2011-11-13
 * Author  : xiongwei
 * Version : 1.0
 */
package com.manle.push.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用途：TODO 
 * @author bbdtek
 */
public class PageParseConfig {
	public String name="";
	public String url="";
	public String urlfromdb="";
	public String urlhead="";
	public Map<String, String> att = new HashMap<String, String>();
	public List<ConfigInfor> infors = new ArrayList<ConfigInfor>();
//	public List<PageParseConfig> listPage = new ArrayList<PageParseConfig>();
	/**
	 * @param string
	 */
	public void setUrl(String url) {
		// TODO Auto-generated method stub
		this.url = url;
	}

}
