package com.tennis.booking.TennisBooking.schedule;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tennis.booking.TennisBooking.utils.ConstantUtil;
import com.tennis.booking.TennisBooking.utils.DateUtil;
import com.tennis.booking.TennisBooking.utils.HttpClientService;
import com.tennis.booking.TennisBooking.utils.SMSSender;

public class QiangThread extends Thread{
	
	private Logger log = Logger.getLogger(QiangThread.class);
	
	private String userid;
	
	private String date;
	
	private String beginTime1;
	
	private String beginTime2;
	
	private QiuChang qiu;

	public QiangThread(String userid, String date, String beginTime1, String beginTime2, QiuChang qiu) {
		this.userid = userid;
		this.date = date;
		this.beginTime2 = beginTime2;
		this.beginTime1 = beginTime1;
		this.qiu = qiu;
	}
	
	@Override
	public void run(){
		//log.info(userid + " 开始抢场地   ，时间 " + date);
		
		xunHuanYuding(date);
		
	}
	
	
	public void xunHuanYuding(String date){
		//log.info(userid + " xun huan yu ding kaishi");
		
		boolean success = false;
		
		String dingdanhao = "";
		String changdi = "";
		
		//List<QiuChang> qius = QiuChangCons.getQiuChangList();
		
		//for (QiuChang qiu: qius) {
			try {
				dingdanhao = suoDan(qiu, date);
			} catch (Exception e) {
				log.error("预定场地 "  + qiu.getParkname() +  " 异常:  " + e.getMessage());
			}
			if (dingdanhao != null && !"".equals(dingdanhao)) {
				success = true;
				changdi = qiu.getParkname();
				//break;
			}
		//}
		
		if (!success) {
//			log.info(userid + " 一个场地都没预定成功, ");
//			try {
//				sendSMS("", false);
//			} catch (Exception e) {
//				log.error("发送失败短信异常");
//			}
		} else {
			if (dingdanhao != null && !"".equals(dingdanhao)) {
				try {
					geiqian(changdi, dingdanhao);
				} catch (Exception e) {
					log.error(changdi + " 是预定成功了， 但给钱失败了， 订单号 " + dingdanhao);
				}
			} else {
				log.info("ding dan hao bu zhi dao wei shen me diu le");
			}
		}
		
	}

	public String suoDan(QiuChang qiu, String date) throws Exception{
		String url = "http://47.92.141.158/TennisCenterInterface/pmPark/addParkOrder.action";

		String parkList = getJson(qiu, date);
		
		Map hashMap = new HashMap();
		hashMap.put("userid", userid);
		hashMap.put("parkList", parkList);
		hashMap.put("paywaycode", "0");
		hashMap.put("addOrderType", "app");

		String timeStamp = (new Date().getTime() / 1000) + "";
		String sign = getSign(timeStamp, hashMap);

		Object[] params = new Object[] {"userid", "parkList", "paywaycode", "addOrderType", "sign", "timestamp" };

		Object[] values = new Object[] {userid, parkList, "0", "app", sign, timeStamp };

		List<NameValuePair> paramsList = HttpClientService.getParams(params, values);
		//Object result = sendPost(url, paramsList);
		sendPostAych(url, paramsList);

		String dingdan = "";
		
//		if (result != null) {
//			//log.info(userid + "  changdi " + qiu.getParkname() + " xia dan data " + JSONObject.toJSONString(result));
//			
//			JSON json = (JSON)JSONObject.toJSON(result);
//			JSONObject ob = (JSONObject)JSONObject.toJSON(json);
//			
//			Integer code = ob.getInteger("respCode");
//			String respMsg = ob.getString("respMsg");
//			
//			if (code == null || code != 1001) {
//				log.error(userid + "  changdi " +  qiu.getParkname()  + " yu ding shibai, msg " + respMsg);
//			} else {
//				JSONObject ob2 = (JSONObject)ob.get("datas");
//				if (ob2 == null) {
//					log.error("no property datas ");
//				} else {
//					log.info(userid + "  changdi " +  qiu.getParkname()  + " yu ding chenggong^^^^^^^^^^^^^^^");
//					dingdan = ob2.getString("orderNo");
//				}
//			}
//		} else {
//			log.info("data is null!!!!");
//		}
		
		return dingdan;
	}
	
	
	
	public void geiqian(String changdi, String dingdan) throws Exception{
		String url = "http://47.92.141.158/TennisCenterInterface/omOrder/payByCard.action";

		Map hashMap = new HashMap();
		hashMap.put("userid", userid);
		hashMap.put("orderNo", dingdan);
		
		String timeStamp = (new Date().getTime() / 1000) + "";
		String sign = getSign(timeStamp, hashMap);

		Object[] params = new Object[] {"userid", "orderNo", "sign", "timestamp"};

		Object[] values = new Object[] {userid, dingdan, sign, timeStamp};

		List<NameValuePair> paramsList = HttpClientService.getParams(params, values);
		Object result = sendPost(url, paramsList);

		if (result != null) {
			log.info("fu kuan data " + result.toString());
			
			JSON json = (JSON)JSONObject.toJSON(result);
			JSONObject ob = (JSONObject)JSONObject.toJSON(json);
			
			Integer code = ob.getInteger("respCode");
			String respMsg = ob.getString("respMsg");
			
			if (code == null || code != 1001) {
				log.error(userid + " gei qian shi bai le, msg " + respMsg);
				sendSMS(",付款失败了", false);
			} else {
				log.info(userid + " gei qian cheng gong le");
				sendSMS(changdi + ", 时间  " + date, true);
			}
		} else {
			log.info("data is null!!!!");
		}
		
		
	}

	private String getJson(QiuChang qiu, String date) {
		
		String qiuname = qiu.getParkname();
		String qiuid = qiu.getParkid();
		

		List<ParkBean> submitList = new ArrayList<>();

		ParkBean firstHourBean = new ParkBean(qiuname, qiuid, beginTime1, date);
		submitList.add(firstHourBean);
		ParkBean secondHourBean = new ParkBean(qiuname, qiuid, beginTime2, date);
		submitList.add(secondHourBean);

		String parkListJson = JSONObject.toJSONString(submitList);

		/*
		 * [ {"parkname" : "C5", "parkid" : "44", "time" : "12", "date" : "2020-07-22"
		 * }, {"parkname" : "C5", "parkid" : "44", "time" : "13", "date" : "2020-07-22"
		 * } ]
		 */
		//log.info("^^^^^^^json: " + parkListJson);
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
	
	public Object sendPost(String url, List<NameValuePair> nameValuePairList) throws Exception {
		JSONObject jsonObject = null;
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		try {
			/**
			 * 创建一个httpclient对象
			 */
			client = HttpClients.createDefault();
			
//			RequestConfig defaultRequestConfig = RequestConfig.custom()
//					.setConnectTimeout(30000) //lian jie chao shi shi jian
//				    .setSocketTimeout(20000) // shu ju fanhui shijian
//				    .setConnectionRequestTimeout(5000)
//				    .build();
			
			/**
			 * 创建一个post对象
			 */
			HttpPost post = new HttpPost(url);
			/**
			 * 包装成一个Entity对象
			 */
			StringEntity entity = new UrlEncodedFormEntity(nameValuePairList, "UTF-8");
			/**
			 * 设置请求的内容
			 */
			post.setEntity(entity);
			/**
			 * 设置请求的报文头部的编码
			 */
			post.setHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"));
			/**
			 * 设置请求的报文头部的编码
			 */
			post.setHeader(new BasicHeader("Accept", "text/plain;charset=utf-8"));
			
//			post.setConfig(defaultRequestConfig);
			/**
			 * 执行post请求
			 */
			response = client.execute(post);
			/**
			 * 获取响应码
			 */
			int statusCode = response.getStatusLine().getStatusCode();
			if (ConstantUtil.SUCCESS_CODE == statusCode) {
				/**
				 * 通过EntityUitls获取返回内容
				 */
				String result = EntityUtils.toString(response.getEntity(), "UTF-8");
				/**
				 * 转换成json,根据合法性返回json或者字符串
				 */
				try {
					jsonObject = JSONObject.parseObject(result);
					return jsonObject;
				} catch (Exception e) {
					return result;
				}
			} else {
				log.error("HttpClientService-line POST请求失败！");
			}
		} catch (Exception e) {
			log.error("HttpClientService-line Exception： " + e.getMessage());
		} finally {
			if (response != null) {
				response.close();
			}
			
			if (client != null) {
				client.close();
			}
		}
		return null;
	}
	
	public void sendPostAych(String url, List<NameValuePair> nameValuePairList) throws Exception {
		CloseableHttpAsyncClient client = null;
		
		
		try {
			/**
			 * 创建一个httpclient对象
			 */
			client = HttpAsyncClients.createDefault();
			
			client.start();
			
			CountDownLatch latch = new CountDownLatch(1);
	
			/**
			 * 创建一个post对象
			 */
			HttpPost post = new HttpPost(url);
			/**
			 * 包装成一个Entity对象
			 */
			StringEntity entity = new UrlEncodedFormEntity(nameValuePairList, "UTF-8");
			/**
			 * 设置请求的内容
			 */
			post.setEntity(entity);
			/**
			 * 设置请求的报文头部的编码
			 */
			post.setHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"));
			/**
			 * 设置请求的报文头部的编码
			 */
			post.setHeader(new BasicHeader("Accept", "text/plain;charset=utf-8"));
			/**
			 * 执行post请求
			 */
			client.execute(post, new FutureCallback<HttpResponse>() {

    	        public void completed(final HttpResponse response) {
    	            latch.countDown();
    	            
    	            int statusCode = response.getStatusLine().getStatusCode();
    				if (ConstantUtil.SUCCESS_CODE == statusCode) {
    					try {
    						String result = EntityUtils.toString(response.getEntity(), "UTF-8");
    						
    						log.info("预定场地 " + qiu.getParkname() + " 返回的result " + result);
    						
    						JSONObject jsonObject = JSONObject.parseObject(result);
    						
    						Integer code = jsonObject.getInteger("respCode");
    						String respMsg = jsonObject.getString("respMsg");
    						
    						if (code == null || code != 1001) {
    							log.error(userid + "  changdi " +  qiu.getParkname()  + " yu ding shibai, msg " + respMsg);
    						} else {
    							JSONObject ob2 = (JSONObject)jsonObject.get("datas");
    							if (ob2 == null) {
    								log.error("no property datas ");
    							} else {
    								log.info(userid + "  changdi " +  qiu.getParkname()  + " yu ding chenggong^^^^^^^^^^^^^^^");
    								String dingdanhao = ob2.getString("orderNo");
    								
    								if (dingdanhao != null && !"".equals(dingdanhao)) {
    									try {
    										geiqian(qiu.getParkname(), dingdanhao);
    									} catch (Exception e) {
    										log.error(qiu.getParkname() + " 是预定成功了， 但给钱失败了， 订单号 " + dingdanhao);
    									}
    								} else {
    									log.info("ding dan hao bu zhi dao wei shen me diu le");
    								}
    								
    							}
    						}
    					} catch (Exception e) {
    						log.error("解析JSON 出错, message " + e.getMessage());
    					}
    				} else {
    					log.error("HttpClientService-line POST请求失败！");
    				}
    				
    	        }

    	        public void failed(final Exception ex) {
    	            latch.countDown();
    	            log.error("HttpClientService-line POST请求失败, message " + ex.getMessage());
    	        }

    	        public void cancelled() {
    	            latch.countDown();
    	            log.error("HttpClientService-line POST请求取消 ");
    	        }

    	    });
    	    latch.await();
			
		} catch (Exception e) {
			log.error("HttpClientService-line Exception： " + e.getMessage());
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}
	
	
	private void sendSMS(String content, boolean success) throws Exception {
		Date d = DateUtil.convertStringToDate(DateUtil.datePattern, date);
		String zhouji = DateUtil.getWeekZhCN(d);
		
//		if ("13397".equals(userid)) {
//			sendRegisterVrifyCode("17710871133", "辛教练"+content, success);
//	        if ("Wed".equals(zhouji)) {
//				
//			}
//		}
//		
//		if ("15837".equals(userid)) {
//			sendRegisterVrifyCode("15321336833", "陶教练"+content, success);
//		}
		
		sendRegisterVrifyCode("13810010934", "娜娜"+content, success);
		sendRegisterVrifyCode("13718656535", "伟伟"+content, success);
		sendRegisterVrifyCode("13911788783", "阿亮"+content, success);
		sendRegisterVrifyCode("18810545732", "欣欣"+content, success);
	}
	
	private void sendRegisterVrifyCode(String mobile, String codeNumber, boolean success) throws Exception {
		log.info("send verify code " + codeNumber + " to "  + mobile);
		SMSSender.SendPost("86", mobile, codeNumber, success);
	}
	
}
