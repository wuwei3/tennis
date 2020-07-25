package com.tennis.booking.TennisBooking.schedule;

public class ParkBean {
	
	private String parkname;
	 
	private String parkid;
	 
	private String time;
	 
	private String date;

	public ParkBean(String parkname, String parkid, String time, String date) {
		super();
		this.parkname = parkname;
		this.parkid = parkid;
		this.time = time;
		this.date = date;
	}

	public String getParkname() {
		return parkname;
	}

	public void setParkname(String parkname) {
		this.parkname = parkname;
	}

	public String getParkid() {
		return parkid;
	}

	public void setParkid(String parkid) {
		this.parkid = parkid;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	 
	 
	 

}
