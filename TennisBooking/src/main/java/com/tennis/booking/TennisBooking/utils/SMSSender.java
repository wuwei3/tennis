package com.tennis.booking.TennisBooking.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;


public class SMSSender {

	static Logger log = Logger.getLogger(SMSSender.class);

	private static String cpid = "p4flkq";

	private static String cppwd = "7uvQXboe";

	/**
	 * Post method to send sms
	 * 
	 * @param url
	 *            http://api2.santo.cc/submit
	 * @param cpid
	 *            - API key
	 * 
	 * @param cppwd
	 *            - API Secret
	 * 
	 * @param da
	 *            - target mobile number
	 * 
	 * @param sm
	 *            - sms conent
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String SendPost(String url, String command, String cpid, String cppwd, String da, String sm,
			String sa) throws Exception {

		log.info("send sms");
		log.info("mobile number is " + da);

		HttpClient client = new HttpClient(new HttpClientParams(), new SimpleHttpConnectionManager());
		PostMethod method = new PostMethod();
		try {
			URI base = new URI(url, false);
			method.setURI(new URI(base, "", false));
			method.setRequestBody(new NameValuePair[] { new NameValuePair("command", command),
					new NameValuePair("cpid", cpid), new NameValuePair("cppwd", cppwd), new NameValuePair("da", da),
					new NameValuePair("sm", sm), new NameValuePair("sa", sa) });
			HttpMethodParams params = new HttpMethodParams();
			params.setContentCharset("UTF-8");
			method.setParams(params);
			int result = client.executeMethod(method);
			if (result == HttpStatus.SC_OK) {
				InputStream in = method.getResponseBodyAsStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = in.read(buffer)) != -1) {
					baos.write(buffer, 0, len);
				}
				in.close();// 1
				baos.close();// 2
				return URLDecoder.decode(baos.toString(), "UTF-8");
			} else {
				log.info("Exception while send sms, message is " + method.getStatusText());
				throw new Exception("HTTP ERROR Status: " + method.getStatusCode() + ":" + method.getStatusText());
			}
		} finally {
			method.releaseConnection();
		}
	}

	public static byte[] hexStr2ByteArr(String strIn) throws Exception {
		byte[] arrB = strIn.getBytes();
		int iLen = arrB.length;
		// 两个字符表示一个字节，所以字节数组长度是字符串长度除以2
		byte[] arrOut = new byte[iLen / 2];
		for (int i = 0; i < iLen; i = i + 2) {
			String strTmp = new String(arrB, i, 2);
			arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);
		}
		return arrOut;
	}

	public static String decodeHexStr(String hexStr) {
		String realStr = null;
		if (hexStr != null) {
			try {
				byte[] data = hexStr2ByteArr(hexStr);
				realStr = new String(data, "GBK");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return realStr;
	}

	public static void SendPost(String countryCode, String mobile, String codenumber, boolean success) throws Exception {
		log.info("open thread to send sms, countrycode " + countryCode + ", mobile " + mobile + ",  codenumber "
				+ codenumber);

		String content = null;
		if (success) {
			content = ConstantUtil.SMS_CONTENT_SUCCESS;
		} else {
			content = ConstantUtil.SMS_CONTENT_FAIL;
		}
		content = content.replace("@", codenumber);
		log.info("final sms content " + content);

		String url = ConstantUtil.SMS_URL;
		String command = ConstantUtil.SMS_COMMAND;
		String targetMobile = countryCode + mobile;

		 new SendSmsThread(url, command, targetMobile, cpid, cppwd,
		 content).start();

//		log.info("mobile " + targetMobile + " send sms");
//		String result = SendPost(url, command, cpid, cppwd, targetMobile, content, null);
//		log.info("send result " + result);
//
//		if (result != null && result.indexOf("mtstat=ACCEPTD&mterrcode=000") > 0) {
//			log.info("send sms to " + targetMobile + " success");
//		}
	}

	public static void main(String[] args) throws Exception {
		String url = "http://api2.santo.cc/submit"; // 应用地址 (无特殊情况时无需修改)
		String command = "MT_REQUEST"; // MT_REQUEST：短信 VO_REQUEST：语音
		String da = "8613718656535"; // 目标号码
										// 如中国大陆：8613800000000(支持多号码批量提交，多个号码之间用半角逗号分隔，最多100个)
		String sm = "您的验证码是:娜娜成功了哟, 5分钟内有效，请不要告诉他人。"; // 短信内容(URL utf-8编码)
		String sa = null; // 自定义发送者号码 (仅限数字或者字母,纯数字支持最大16个字符,带有字母支持最大11个字符)

		try {
			String returnString2 = SendPost(url, command, cpid, cppwd, da, sm, sa);
			System.out.println(returnString2);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
