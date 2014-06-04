package com.manle.push.ios;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.manle.push.utils.Config;
import com.manle.push.utils.DBUtil;
import com.manle.push.utils.DateUtil;

import javapns.Push;
import javapns.devices.Device;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import javapns.notification.PushedNotifications;

public class PushMain {
	/**
	 * send message to apns
	 * 
	 * @throws Exception
	 */
	public static PushedNotifications send(String alert, String url, String module, 
			String type, Long time, String id, List<Device> devices) throws Exception {
		PushNotificationPayload payload = PushNotificationPayload.complex();
		String keystore = "res/aio_push.p12"; // P12文件的路径；
		String password = "hk123"; // P12文件的密码；
		boolean production = true; // 设置true为正式服务地址，false为开发者地址

		payload.addAlert(alert);
		// payload.addBadge(1);
		payload.addCustomDictionary("url", url);
		payload.addCustomDictionary("type", type);
		payload.addCustomDictionary("module", module);
		payload.addSound("default");
		payload.addCustomDictionary("timestamp", time+"");
		payload.addCustomDictionary("id", id);

		return Push.payload(payload, keystore, password, production, devices);
	}

	public static void printPushedNotifications(String description,
			List<PushedNotification> notifications) {
		System.out.println(description);
		for (PushedNotification notification : notifications) {
			try {
				System.out.println("  " + notification.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void printPushedNotifications(
			List<PushedNotification> notifications) {
		List<PushedNotification> failedNotifications = PushedNotification
				.findFailedNotifications(notifications);
		List<PushedNotification> successfulNotifications = PushedNotification
				.findSuccessfulNotifications(notifications);
		int failed = failedNotifications.size();
		int successful = successfulNotifications.size();

		if (successful > 0 && failed == 0) {
			printPushedNotifications("All notifications pushed successfully ("
					+ successfulNotifications.size() + "):",
					successfulNotifications);
		} else if (successful == 0 && failed > 0) {
			printPushedNotifications("All notifications failed ("
					+ failedNotifications.size() + "):", failedNotifications);
		} else if (successful == 0 && failed == 0) {
			System.out.println("No notifications could be sent, probably because of a critical error");
		} else {
			printPushedNotifications("Some notifications failed ("
					+ failedNotifications.size() + "):", failedNotifications);
			printPushedNotifications("Others succeeded ("
					+ successfulNotifications.size() + "):",
					successfulNotifications);
		}
	}
	
	public static void uploadPushedNotifications(List<PushedNotification> notifications, String connf, String table, String msgID, String app_id) {
		List<PushedNotification> failedNotifications = PushedNotification
				.findFailedNotifications(notifications);
		List<PushedNotification> successfulNotifications = PushedNotification
				.findSuccessfulNotifications(notifications);
		int failed = failedNotifications.size();
		int successful = successfulNotifications.size();

		if(successful > 0){
			updateMsgTable(connf, table, msgID, app_id, successful);
			
		}
		
	}
	
	public static void updateMsgTable(String connFac, String table, String msg_id, String app_id ,int success){
		String sql = "select count(1) from "+ table 
				+ " where id = " + msg_id + " and app_id = '" + app_id +"'";
		System.out.println(sql);
		List<Map<String, Object>> ret = DBUtil.sqlExec(sql, connFac);
		if(null == ret || ret.size() <= 0)
			return;
		else {
			sql = "update " + table +" set valid = 0,  recv_num = recv_num+ "+success 
				+ " where id = "+msg_id+ " and app_id = '" +app_id +"'";
			System.out.println(sql);
			DBUtil.exec(sql, connFac);
			
		}
	}

	public static void main(String[] args) {
		String app_id = "2";
		long currTime = DateUtil.getCurTimestamp();
		
		// 初始化数据库
		Config.init("xml/push.xml");
		String ConnFac = "snswebbus-203";
		String msgTable = "pre_phone_ios_push_msg";
		String deviceTable = "pre_phone_ios_device_info";
		String unPushedDeviceTable = "pre_phone_ios_unpushed";
		
		// 从推送数据库中获得消息
		String sql = "SELECT * FROM " + msgTable
				+ " WHERE valid = 1 AND app_id = " + app_id 
				+ " AND begin_time <= " + currTime + " AND expiration_time >=" + currTime
				+ " ORDER BY id ASC";
		List<Map<String, Object>> ret = DBUtil.sqlExec(sql, ConnFac);
		if (null == ret || ret.size() <= 0) {
			return;
		}

		for (Map<String, Object> msg : ret) {
			String alert = (String) msg.get("msg_title");
			String url = (String) msg.get("msg_url");
			String type = (String) msg.get("type");
			String module = (String) msg.get("module");
			String minVersion = (String) msg.get("min_version");
			String maxVersion = (String) msg.get("max_version");

			String id = (String) msg.get("id").toString();

			// 从device_info中都去deviceToken
			sql = "SELECT DISTINCT device_token FROM " + deviceTable
					+ " WHERE app_id = " + app_id + " AND device_token <> ''";
//					+ " AND app_version >='" +minVersion+ "' AND app_version <='"+maxVersion+"'";
			ret = DBUtil.sqlExec(sql, ConnFac);
			if (null == ret || ret.size() <= 0) {
				return;
			}

			List<Device> devices = new ArrayList<Device>();
			
			// 向苹果服务器发送消息
//			BasicDevice device = null;
//			String token = "7b164e7d84d8b6316f538193acc707a3590b7f38556919085594ca10d1e2600b";
//			try {
//				device = new BasicDevice(token);
//			} catch (InvalidDeviceTokenFormatException e1) {
//				e1.printStackTrace();
//				continue;
//			}
//
//			devices.add(device);
			
			for (Map<String, Object> tokenMap : ret) {
				String token = (String) tokenMap.get("device_token");
				BasicDevice device = null;
				try {
					device = new BasicDevice(token) ;
				} catch (InvalidDeviceTokenFormatException e1) {
					e1.printStackTrace();
					continue;
				}
				
				devices.add(device);
			}
			try {
//				PushedNotifications result = send("test_title", "http://www.manle.com", "aa", currTime, "1000", devices);
				PushedNotifications result = send(alert, url, module, type, currTime, id, devices);
				uploadPushedNotifications(result, ConnFac, msgTable, id, app_id);
			} catch (Exception e) { // 将剩下的token放到未推送表中
				e.printStackTrace();
			}
		}

	}
}
