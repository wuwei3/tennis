package com.tennis.booking.TennisBooking.utils;

import java.io.File;

import org.apache.log4j.RollingFileAppender;

public class Log4jUtil extends RollingFileAppender{
	
	@Override
    public void setFile(String file) {
        String filePath = file;
        File fileCheck = new File(filePath);
        if (!fileCheck.exists())
            fileCheck.getParentFile().mkdirs();
        super.setFile(filePath);
    }

}
