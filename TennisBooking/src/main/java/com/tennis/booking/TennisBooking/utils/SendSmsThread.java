package com.tennis.booking.TennisBooking.utils;

import org.apache.log4j.Logger;

public class SendSmsThread extends Thread{

	static Logger log = Logger.getLogger(SendSmsThread.class);
	
	private String url;
	
	private String command;
	
	private String targetMobile;
	
	private String cpid;
	
	private String cppwd;
	
	private String content;
	
	public SendSmsThread(String url, String command, String targetMobile, String cpid, String cppwd, String content) {
		this.url = url;
		this.command = command;
		this.targetMobile = targetMobile;
		this.cpid = cpid;
		this.cppwd = cppwd;
		this.content = content;
	}
	
	@Override
	public void run() {
		try {
			log.info("mobile " + targetMobile + " send sms");
			String result = SMSSender.SendPost(url,command,cpid, cppwd,targetMobile,content,null);
			log.info("send result " + result);
			
			if (result != null && result.indexOf("mtstat=ACCEPTD&mterrcode=000") > 0) {
				log.info("send sms to " + targetMobile  + " success");
			}
		} catch (Exception e) {
			log.info("send sms has exception!!!!!!, mobile " + targetMobile + ",  msg " + e.getMessage());
		}
	}
	
}
