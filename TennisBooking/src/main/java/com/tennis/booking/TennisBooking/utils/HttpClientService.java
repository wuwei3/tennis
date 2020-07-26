package com.tennis.booking.TennisBooking.utils;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class HttpClientService {

	private static final Logger LOGGER = Logger.getLogger(HttpClientService.class);

	private static final int SUCCESS_CODE = 200;

	public static Object sendGet(String url, List<NameValuePair> nameValuePairList) throws Exception {
		JSONObject jsonObject = null;
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		try {
			/**
			 * 创建HttpClient对象
			 */
			client = HttpClients.createDefault();
			/**
			 * 创建URIBuilder
			 */
			URIBuilder uriBuilder = new URIBuilder(url);
			/**
			 * 设置参数
			 */
			uriBuilder.addParameters(nameValuePairList);
			/**
			 * 创建HttpGet
			 */
			HttpGet httpGet = new HttpGet(uriBuilder.build());
			/**
			 * 设置请求头部编码
			 */
			httpGet.setHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"));
			/**
			 * 设置返回编码
			 */
			httpGet.setHeader(new BasicHeader("Accept", "text/plain;charset=utf-8"));
			/**
			 * 请求服务
			 */
			response = client.execute(httpGet);
			/**
			 * 获取响应吗
			 */
			int statusCode = response.getStatusLine().getStatusCode();

			if (SUCCESS_CODE == statusCode) {
				/**
				 * 获取返回对象
				 */
				HttpEntity entity = response.getEntity();
				/**
				 * 通过EntityUitls获取返回内容
				 */
				String result = EntityUtils.toString(entity, "UTF-8");
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
				LOGGER.error("HttpClientService-line: GET请求失败！");
			}
		} catch (Exception e) {
			LOGGER.error("HttpClientService-line, exception" + e.getMessage());
		} finally {
			response.close();
			client.close();
		}
		return null;
	}

	public static Object sendPost(String url, List<NameValuePair> nameValuePairList) throws Exception {
		JSONObject jsonObject = null;
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		try {
			/**
			 * 创建一个httpclient对象
			 */
			client = HttpClients.createDefault();
			
			RequestConfig defaultRequestConfig = RequestConfig.custom()
					.setConnectTimeout(5000) //lian jie chao shi shi jian
				    .setSocketTimeout(10000) // shu ju fanhui shijian
				    .setConnectionRequestTimeout(5000)
				    .build();
			
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
			
			post.setConfig(defaultRequestConfig);
			/**
			 * 执行post请求
			 */
			response = client.execute(post);
			/**
			 * 获取响应码
			 */
			int statusCode = response.getStatusLine().getStatusCode();
			if (SUCCESS_CODE == statusCode) {
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
				LOGGER.error("HttpClientService-line POST请求失败！");
			}
		} catch (Exception e) {
			LOGGER.error("HttpClientService-line Exception： " + e.getMessage());
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

	/**
	 * 组织请求参数{参数名和参数值下标保持一致}
	 * 
	 * @param params 参数名数组
	 * @param values 参数值数组
	 * @return 参数对象
	 */
	public static List<NameValuePair> getParams(Object[] params, Object[] values) {
		/**
		 * 校验参数合法性
		 */
		boolean flag = params.length > 0 && values.length > 0 && params.length == values.length;
		if (flag) {
			List<NameValuePair> nameValuePairList = new ArrayList<>();
			for (int i = 0; i < params.length; i++) {
				nameValuePairList.add(new BasicNameValuePair(params[i].toString(), values[i].toString()));
			}
			return nameValuePairList;
		} else {
			LOGGER.error("HttpClientService-line: 请求参数为空且参数长度不一致");
		}
		return null;
	}
	
	private static String getSign(String timeStamp, Map<String, String> map) {
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
	
	public static void main(String[] args) throws Exception{
        String url = "http://tennis.bjofp.cn/TennisCenterInterface/pmPark/getParkShowByParam.action";
        
        Map hashMap = new HashMap();
		//hashMap.put("version_code", "3.0");
		hashMap.put("date", "2020-07-23");
		hashMap.put("parkstatues", "0");
		hashMap.put("cardtypecode", "-1");
		hashMap.put("ballcode", "1");
		hashMap.put("parktypeinfo", "1");
		hashMap.put("userid", "22737");

		String timeStamp = (new Date().getTime() / 1000) + "";
		String sign = getSign(timeStamp, hashMap);
		
		Object [] params = new Object[]{"date","parkstatues","cardtypecode","ballcode","parktypeinfo","userid", "sign","timestamp"};
		
		Object [] values = new Object[]{"2020-07-23","0","-1","1","1","22737",sign,timeStamp};
		
		List<NameValuePair> paramsList = HttpClientService.getParams(params, values);
		
		Object result = sendGet(url, paramsList);
		
		if (result != null) {
			LOGGER.info("data " + result.toString());
			
			JSON json = (JSON)JSONObject.toJSON(result);
			
			//LoginSessionResp login = JSONObject.toJavaObject(json, LoginSessionResp.class);
			
			System.out.println(JSONObject.toJSONString(json));
			
			//FRRealTimeData f = JSONObject.toJavaObject(json, FRRealTimeData.class);
		} else {
			LOGGER.info("data is null!!!!");
		}
	}

}
