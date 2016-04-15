package com.server.modules;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Place {
	private String place_code;
	private String service_online_status;
	private String data_online_status;
	private String end_online_time;
	private String last_data_time;
	public void getFromResult(ResultSet rs) throws SQLException {
		this.place_code = rs.getString("place_code");
		this.service_online_status = rs.getString("service_online_status");
		this.data_online_status = rs.getString("data_online_status");
		this.end_online_time = rs.getString("end_online_time");
		this.last_data_time = rs.getString("last_data_time");
	}
	public String getPlace_code() {
		return place_code;
	}
	public String getService_online_status() {
		return service_online_status;
	}
	public String getData_online_status() {
		return data_online_status;
	}
	public String getEnd_online_time() {
		return end_online_time;
	}
	public String getLast_data_time() {
		return last_data_time;
	}
}
