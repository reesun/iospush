package com.manle.push.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	public Date getCurDate() {
		return new Date(System.currentTimeMillis());
	}
	
	public String getCurDate(SimpleDateFormat sdf) {
		return sdf.format(getCurDate());
	}
	
	public Date getYesterday(){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(getCurDate());
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		return calendar.getTime();
	}
	
	public String getYesterday(SimpleDateFormat sdf) {
		return sdf.format(getYesterday());

	}
	
	public SimpleDateFormat getSimpleDateFormat (String str) {
		return new SimpleDateFormat(str);
	}
	
	public static long getCurTimestamp(){
		return System.currentTimeMillis()/1000;
	}
	
	public static void main(String[] args){
		System.out.println(getCurTimestamp());
	}
}
