package com.tennis.booking.TennisBooking.schedule;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.http.NameValuePair;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tennis.booking.TennisBooking.utils.DateUtil;
import com.tennis.booking.TennisBooking.utils.HttpClientService;
import com.tennis.booking.TennisBooking.utils.SMSSender;

@Component
public class ScheduleTask {

	private Logger log = Logger.getLogger(ScheduleTask.class);

//	@Scheduled(cron="0 33 14 * * ?")
//	public void setStatusToProcessing() throws Exception {
//		log.info("kai shi");
//		
//		String date = "2020-06-22";
//		updateOrderService.updateOrderStatus(OrderStatus.PROCEEDING.getName(), date);
//	}
//	
	
//	@Scheduled(cron="02 0 0 ? * L")
	@Scheduled(cron="0 0 0 * * ?")
	public void setStatusToFinished() throws Exception {
		log.info("kai shi222");
		
		//String zhousan = getYuDingDate();
		
//		xunHuanYuding(zhousan);
		
//		String dingdan = suoDan(zhousan);
//		
//		if (dingdan != null && !"".equals(dingdan)) {
//			geiqian(dingdan);
//		} else {
//			log.error("xia dan shi shibai de!!!!!!!!!!!");
//		}
		
		String threedays = get3DayLater();
		String[] userids = {"13397", "15837"};
		for (String userid: userids) {
			new QiangThread(userid, threedays, "19", "20").start();
		}
	}
	
	private String get3DayLater() throws Exception{
		Calendar calendar2 = Calendar.getInstance();
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
		calendar2.add(Calendar.DATE, 3);
		String three_days_after = sdf2.format(calendar2.getTime());
		System.out.println("3 tian hou date  " + three_days_after);
		
		return three_days_after;
	}
	
	
	private String getYuDingDate() throws Exception{
		Date d = new Date();
		String date = DateUtil.getDate(DateUtil.datePattern, d);
		
		Map<String, String> we = DateUtil.getNextWeek(date);
		for(Map.Entry<String, String> entry : we.entrySet()){
		    String mapKey = entry.getKey();
		    String mapValue = entry.getValue();
		    System.out.println(mapKey+" : "+mapValue);
		}
		
		String begin = we.get("beginDateNext");
		String end = we.get("endDateNext");
		
		String[] sa = DateUtil.getDatesBetweenStartEnd(begin, end);
		
		String zhousan = sa[2];
		
		log.info("yuding date " + zhousan);
		
		return zhousan;
	}
	
	public void xunHuanYuding(String date) throws Exception{
		log.info("xun huan yu ding kaishi");
		
		boolean success = false;
		
		String dingdanhao = "";
		
		List<QiuChang> qius = QiuChangCons.getQiuChangList();
		
		for (QiuChang qiu: qius) {
			dingdanhao = suoDan(qiu, date);
			if (dingdanhao != null && !"".equals(dingdanhao)) {
				success = true;
				break;
			}
		}
		
		if (!success) {
			log.info("一个场地都没预定成功, ");
			sendSMS("4444");
		} else {
			if (dingdanhao != null && !"".equals(dingdanhao)) {
				geiqian(dingdanhao);
			} else {
				log.info("ding dan hao bu zhi dao wei shen me diu le");
			}
		}
		
	}

	public String suoDan(QiuChang qiu, String date) throws Exception{
		String url = "http://tennis.bjofp.cn/TennisCenterInterface/pmPark/addParkOrder.action";

		String parkList = getJson(qiu, date);
		
		Map hashMap = new HashMap();
		hashMap.put("userid", "13397");
		hashMap.put("parkList", parkList);
		hashMap.put("paywaycode", "0");
		hashMap.put("addOrderType", "app");

		String timeStamp = (new Date().getTime() / 1000) + "";
		String sign = getSign(timeStamp, hashMap);

		Object[] params = new Object[] {"userid", "parkList", "paywaycode", "addOrderType", "sign", "timestamp" };

		Object[] values = new Object[] { "13397", parkList, "0", "app", sign, timeStamp };

		List<NameValuePair> paramsList = HttpClientService.getParams(params, values);
		Object result = HttpClientService.sendPost(url, paramsList);

		String dingdan = "";
		
		if (result != null) {
			log.info(qiu.getParkname() + " xia dan data " + JSONObject.toJSONString(result));
			
			JSON json = (JSON)JSONObject.toJSON(result);
			JSONObject ob = (JSONObject)JSONObject.toJSON(json);
			
			Integer code = ob.getInteger("respCode");
			String respMsg = ob.getString("respMsg");
			
			if (code == null || code != 1001) {
				log.error(qiu.getParkname()  + " yu ding shibai, msg " + respMsg);
			} else {
				JSONObject ob2 = (JSONObject)ob.get("datas");
				if (ob2 == null) {
					log.error("no property datas ");
				} else {
					log.info(qiu.getParkname()  + " yu ding chenggong^^^^^^^^^^^^^^^");
					dingdan = ob2.getString("orderNo");
				}
			}
		} else {
			log.info("data is null!!!!");
		}
		
		return dingdan;
	}
	
	
	
	public void geiqian(String dingdan) throws Exception{
		String url = "http://tennis.bjofp.cn/TennisCenterInterface/omOrder/payByCard.action";

		Map hashMap = new HashMap();
		hashMap.put("userid", "13397");
		hashMap.put("orderNo", dingdan);
		
		String timeStamp = (new Date().getTime() / 1000) + "";
		String sign = getSign(timeStamp, hashMap);

		Object[] params = new Object[] {"userid", "orderNo", "sign", "timestamp"};

		Object[] values = new Object[] {"13397", dingdan, sign, timeStamp};

		List<NameValuePair> paramsList = HttpClientService.getParams(params, values);
		Object result = HttpClientService.sendPost(url, paramsList);

		if (result != null) {
			log.info("fu kuan data " + result.toString());
			
			JSON json = (JSON)JSONObject.toJSON(result);
			JSONObject ob = (JSONObject)JSONObject.toJSON(json);
			
			Integer code = ob.getInteger("respCode");
			String respMsg = ob.getString("respMsg");
			
			if (code == null || code != 1001) {
				log.error("gei qian shi bai le, msg " + respMsg);
				sendSMS("5555");
			} else {
				log.info(" gei qian cheng gong le");
				sendSMS("6666");
			}
		} else {
			log.info("data is null!!!!");
		}
		
		
	}

	private String getJson(QiuChang qiu, String date) {
		
		String qiuname = qiu.getParkname();
		String qiuid = qiu.getParkid();
		

		List<ParkBean> submitList = new ArrayList<>();

		ParkBean firstHourBean = new ParkBean(qiuname, qiuid, "19", date);
		submitList.add(firstHourBean);
		ParkBean secondHourBean = new ParkBean(qiuname, qiuid, "20", date);
		submitList.add(secondHourBean);

		String parkListJson = JSONObject.toJSONString(submitList);

		/*
		 * [ {"parkname" : "C5", "parkid" : "44", "time" : "12", "date" : "2020-07-22"
		 * }, {"parkname" : "C5", "parkid" : "44", "time" : "13", "date" : "2020-07-22"
		 * } ]
		 */
		log.info("^^^^^^^json: " + parkListJson);
		return parkListJson;
	}

	private String getSign(String timeStamp, Map<String, String> map) {
		TreeMap treeMap = new TreeMap();
		if (map != null) {
			for (String next : map.keySet()) {
				treeMap.put(next, String.valueOf(map.get(next)));
			}
		}
		treeMap.put("secret", "37d67faf32fd0309e7ef149510ee6c74");
		treeMap.put("timestamp", timeStamp);
		return createSign(treeMap);
	}

	private static String createSign(SortedMap<String, String> sortedMap) {
		StringBuffer stringBuffer = new StringBuffer();
		for (Map.Entry next : sortedMap.entrySet()) {
			String str = (String) next.getKey();
			String str2 = (String) next.getValue();
			if (str2 != null && !"".equals(str2) && !"sign".equals(str) && !"key".equals(str)) {
				stringBuffer.append(str + "=" + str2 + "&");
			}
		}
		stringBuffer.append("key=tenniswxpay3ewr23dsfsdfsweredfsd");
		return md5Encode(stringBuffer.toString()).toUpperCase();
	}

	private static String md5Encode(String str) {
		try {
			MessageDigest instance = MessageDigest.getInstance("MD5");
			instance.update(str.getBytes("UTF-8"));
			byte[] digest = instance.digest();
			StringBuilder sb = new StringBuilder();
			int length = digest.length;
			for (int i = 0; i < length; i++) {
				sb.append(String.format("%02X", new Object[] { Byte.valueOf(digest[i]) }));
			}
			return sb.toString().toLowerCase();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	private void sendRegisterVrifyCode(String mobile, String codeNumber) throws Exception {
		log.info("send verify code " + codeNumber + " to "  + mobile);
		//SMSSender.SendPost("86", mobile, codeNumber);
	}
	
	private void sendSMS(String code) throws Exception {
		sendRegisterVrifyCode("13718656535", code);
		sendRegisterVrifyCode("18810545732", code);
		sendRegisterVrifyCode("13911788783", code);
		sendRegisterVrifyCode("13810010934", code);
	}

}
