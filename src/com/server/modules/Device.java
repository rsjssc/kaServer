package com.server.modules;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Device {
	private String device_code;
	private String place_code;
	private String area_code;
	private String service_online_status;
	private String data_online_status;
	private String last_online_time;
	private String last_data_time;
	private String device_longitude;
	private String device_latitude;
	
	public void getFromResult(ResultSet rs) throws SQLException{
		this.device_code = rs.getString("device_code");
		this.place_code = rs.getString("place_code");
		this.area_code = rs.getString("area_code");
		this.service_online_status = rs.getString("service_online_status");
		this.data_online_status = rs.getString("data_online_status");
		this.last_online_time = rs.getString("last_online_time");
		this.last_data_time = rs.getString("last_data_time");
		this.device_longitude = rs.getString("device_longitude");
		this.device_latitude = rs.getString("device_latitude");
	}
	
	public String getDevice_code() {
		return device_code;
	}
	public String getPlace_code() {
		return place_code;
	}
	public String getArea_code() {
		return area_code;
	}
	public String getService_online_status() {
		return service_online_status;
	}
	public String getData_online_status() {
		return data_online_status;
	}
	public String getLast_online_time() {
		return last_online_time;
	}
	public String getLast_data_time() {
		return last_data_time;
	}
	public String getDevice_longitude() {
		return device_longitude;
	}
	public String getDevice_latitude() {
		return device_latitude;
	}
}
