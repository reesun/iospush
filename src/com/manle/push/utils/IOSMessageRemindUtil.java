package com.manle.push.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.notification.AppleNotificationServer;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.PayloadPerDevice;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotifications;
import javapns.notification.transmission.NotificationProgressListener;
import javapns.notification.transmission.NotificationThread;
import javapns.notification.transmission.NotificationThreads;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;


/**
 * apple消息推送提醒工具类
 * @author huangxiran
 *
 */
public class IOSMessageRemindUtil {
	
	private static Log log = LogFactory.getLog(IOSMessageRemindUtil.class);
	
	public static List<PayloadPerDevice> deviceList;
	
	public static final String TOTAL_STR="[##total@@]";
	
	private static String keystore ;//证书路径和证书名
	private static String password ; // 证书密码
	private static boolean production; // 设置true为正式服务地址，false为开发者地址	
	
	 // 建立与Apple服务器连接
    private static AppleNotificationServer server ;
    
    static {
		ResourceBundle bundle = ResourceBundle.getBundle("javapns");
		keystore = bundle.getString("keystore");
		password = bundle.getString("password");
		production = "true".equals(bundle.getString("production"));
	}
    
	public static void resetDeviceList(){
		deviceList = new ArrayList<PayloadPerDevice>();
	}
	
	/**
	 * 添加一个设备
	 * @param count
	 * @param token
	 * @param content
	 */
	public static void addPayloadPerDevice(int count,String token,String content,List<String> dictionaryList)throws Exception{
		 
			if(deviceList==null){
				resetDeviceList();
			}
			
			if(token==null || content==null){
				return;
			}
			
			PayloadPerDevice payloadPerDevice = setPayloadPerDevice(count,token, content,dictionaryList);
			deviceList.add(payloadPerDevice);
		  
	}

	/**
	 * 设置提醒的内容
	 * @param count
	 * @param token
	 * @param content
	 * @param dictionaryList  字典值，key和value用@ 来分隔
	 * @return
	 * @throws JSONException
	 * @throws InvalidDeviceTokenFormatException
	 */
	private static PayloadPerDevice setPayloadPerDevice(int count,
			String token, String content,List<String> dictionaryList) throws JSONException,
			InvalidDeviceTokenFormatException {
		PushNotificationPayload payload = new PushNotificationPayload();
		payload.addAlert(content);
		payload.addSound("ping.caf");// 声音
		payload.addBadge(count);//图标小红圈的数值
		
		for (Iterator iterator = dictionaryList.iterator(); iterator.hasNext();) {
			String str = (String) iterator.next();
			String[] strs = str.split("@");
			if(strs.length!=2) continue;
			String key = strs[0];
			if("id".equals(key)){
				int value = Integer.parseInt(strs[1]);
				payload.addCustomDictionary(key,value);// 添加字典 
				continue;
			}
			if("timestamp".equals(key)){
				int value = Integer.parseInt(strs[1]);
				payload.addCustomDictionary(key,value);// 添加字典 
				continue;
			}
			String value = strs[1];
			
			payload.addCustomDictionary(key,value);// 添加字典 
			
		}
		PayloadPerDevice payloadPerDevice = new PayloadPerDevice(payload,token);// 将要推送的消息和手机唯一标识绑定
		
		return payloadPerDevice;
	}
	
	/**
	 * 启动多个线程的提醒
	 */
	public static Map<String,PushedNotifications> runNotificationThreads()throws Exception{
		Map<String,PushedNotifications> map = null;
	 
			if(server==null){
				server = new AppleNotificationServerBasicImpl(keystore, password, production);
			}
			if(deviceList==null || deviceList.isEmpty())return null;
			
		    NotificationThreads work = new NotificationThreads(server,deviceList,deviceList.size());// 
			work.setListener(DEBUGGING_PROGRESS_LISTENER);// 对线程的监听，一定要加上这个监听
			work.start(); // 启动线程
			work.waitForAllThreads();// 等待所有线程启动完成
			resetDeviceList();
			map = new HashMap<String,PushedNotifications>();
			map.put("SUCCESS", work.getSuccessfulNotifications());
			map.put("FAILED", work.getFailedNotifications());
			work.clearPushedNotifications();
			work.getThreads().clear();
			work = null;
			System.gc();
		return map;
	}
	
	/**
	 * 启动一个线程的提醒
	 * @param count
	 * @param token
	 * @param content
	 */
	public static void runNotificationThread(List<PayloadPerDevice> devices){
		try {
			if(server==null){
				server = new AppleNotificationServerBasicImpl(keystore, password, production  );
			}
			if(deviceList==null || deviceList.isEmpty()){
				deviceList = devices;
				runNotificationThreads();
				//1、重置设备数量
				resetDeviceList(); 
			}else{
				 //假若已经跑了线程了，该deviceList不为空的时候，直接再把devices再包括进去
				deviceList.containsAll(devices);
			}
		 } catch (Exception e) {
//			e.printStackTrace();
			 log.error("消息推送失败："+e.getMessage());
		}
	}
	
	
	
	// 线程监听
	public static final NotificationProgressListener DEBUGGING_PROGRESS_LISTENER = new NotificationProgressListener() {
			public void eventThreadStarted(NotificationThread notificationThread) {
				System.out.println("   [EVENT]: thread #" + notificationThread.getThreadNumber() + " started with " + " devices beginning at message id #" + notificationThread.getFirstMessageIdentifier());
			}
			public void eventThreadFinished(NotificationThread thread) {
				System.out.println("   [EVENT]: thread #" + thread.getThreadNumber() + " finished: pushed messages #" + thread.getFirstMessageIdentifier() + " to " + thread.getLastMessageIdentifier() + " toward "+ " devices");
			}
			public void eventConnectionRestarted(NotificationThread thread) {
				System.out.println("   [EVENT]: connection restarted in thread #" + thread.getThreadNumber() + " because it reached " + thread.getMaxNotificationsPerConnection() + " notifications per connection");
			}
			public void eventAllThreadsStarted(NotificationThreads notificationThreads) {
				System.out.println("   [EVENT]: all threads started: " + notificationThreads.getThreads().size());
			}
			public void eventAllThreadsFinished(NotificationThreads notificationThreads) {
				System.out.println("   [EVENT]: all threads finished: " + notificationThreads.getThreads().size());
			}
			public void eventCriticalException(NotificationThread notificationThread, Exception exception) {
				System.out.println("   [EVENT]: critical exception occurred: " + exception);
				log.error("推送消息失败："+exception);
			}
	};
	
	
	/**
	 * 根据Token发送推送消息
	 * @param staffCode
	 * @param content
	 * @throws BusinessException 
	 */
	public static void remindMessage(String token,String content,int count) {
		try{
			List<PayloadPerDevice> devices = new ArrayList<PayloadPerDevice>();
			content = content.replace(TOTAL_STR, String.valueOf(count));
			//2、添加设备
			List<String> dictionaryList = new ArrayList<String>();
			dictionaryList.add("dictionary1@"+1);
			dictionaryList.add("dictionary2@"+2);
			dictionaryList.add("dictionary3@"+3);
			
			PayloadPerDevice payloadPerDevice = setPayloadPerDevice(count,token, content,dictionaryList);
			devices.add(payloadPerDevice);
				
			//3、启动设备
			runNotificationThread(devices);
		}catch (Exception e) {
			e.printStackTrace();
			 log.error("消息推送失败："+e.getMessage());
		}
	 
	}
	
 
	 
	 
	
	public static void main(String[] args) {
		try{
			String token = "2847a1af27d521bfd88c2a3de6320ff60f2de26403f4caf1b97e659d7efcc8fe";
			//			String token = "de3574ac0732b56441d1e925565fe307010d7e3fa87d4dbe619b8dc62f0d88cc";//yawei
//			String token = "68dab0ee3d540b108d6e76c012d329802ded4661e6ce5d359f3e690b4a0bcaff";//姚勤
			String content = "123456789false~~~~";
			IOSMessageRemindUtil.remindMessage(token, content, 1);
		}catch (Exception e) {
		}
	}
}
