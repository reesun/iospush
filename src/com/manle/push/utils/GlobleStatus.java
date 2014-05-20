/**
 * Project : ParserToolXD
 * FileName: GlobleStatus.java
 * Created : 2011-11-18
 * Author  : xiongwei
 * Version : 1.0
 */
package com.manle.push.utils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 用途：TODO 
 * @author bbdtek
 */
public class GlobleStatus {
	private static Logger log = LoggerFactory.getLogger(GlobleStatus.class);
	private static GlobleStatus gs = null;
	private boolean isDone = false;
	private List<Object> listThreadCount = new ArrayList<Object>();
	private GlobleStatus(){
		
	}
	synchronized public static GlobleStatus getGs(){
		if(gs == null){
			gs = new GlobleStatus();
		}
		return gs;
	}
	public void beginNewThread(){
		listThreadCount.add(1);
	}
	public void doneThread(){
		if(listThreadCount.size()>0){
			listThreadCount.remove(0);
			log.info(""+listThreadCount.size());
		}else{
			log.error("globleStatus thread counting exception..");
		}
		
	}
	public boolean isAllThreadDone(){
		return (listThreadCount.size()==0);
	}
}
