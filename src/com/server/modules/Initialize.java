package com.server.modules;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.database.DBBridge;
import com.util.PubPara;

import redis.clients.jedis.Jedis;

public class Initialize {
	private static Logger log = Logger.getLogger(Initialize.class);

	public void readFromPostgresql() {
		// TODO Auto-generated method stub
		Jedis jedis = new Jedis(PubPara.strRedisIP, PubPara.nRedisPort);
		//这句会在初始化时清除redis中所有的key-value
		jedis.flushAll();
		
		String sql = null;
		DBBridge db = null;
		try {
			//site 场所信息
			List<Place> places = new ArrayList<Place>();
			try {
				db = new DBBridge();
				db.openBridge();
				log.info("读入场所信息");
				sql = "select * from tbl_site;";//site这张表需要读入的信息由有
				ResultSet rs = db.execSELECT(sql);//执行sql语句
				while(rs.next()) {
					Place new_place = new Place();
					new_place.getFromResult(rs);
					places.add(new_place);
				}
			} catch (Exception e) {
				log.error("数据库操作错误：",e);
			} finally {
				try {
					db.closeBridge();
				} catch (Exception e2) {
					log.error("异常：", e2);
				}
			}
			
			Iterator<Place> PlaceItor = places.iterator();
			int site_count = 0;
			while(PlaceItor.hasNext()) {
				Place place = PlaceItor.next();
				String place_code = place.getPlace_code();
				if (place_code == null) {
					log.error("place_code cannot be null");
					continue;
				}
				site_count++;
				String redis_site_key = KeyExchange.getRedisSiteKeyFromPlaceCode(place_code);
				Map<String, String> map = new HashMap<String, String>();
				map.put("PlaceCode", place.getPlace_code());
				
				//表定义中如果没有默认值,在这里赋初值0
				String OnlineStatus = place.getService_online_status();
				String DataStatus = place.getData_online_status();
				String lastRefreshTime = place.getEnd_online_time();
				String lastDataRefreshTime = place.getLast_data_time();
				if (OnlineStatus == null) {
					OnlineStatus = "0";
				}
				if (DataStatus == null) {
					DataStatus = "0";
				}
				if (lastRefreshTime == null) {
					lastRefreshTime = "0";
				}
				if (lastDataRefreshTime == null) {
					lastDataRefreshTime = "0";
				}
				map.put("OnlineStatus", OnlineStatus);
				map.put("DataStatus", DataStatus);
				map.put("lastRefreshTime", lastRefreshTime);
				map.put("lastDataRefreshTime", lastDataRefreshTime);
				jedis.hmset(redis_site_key, map);
				
				//将设备加到一个装着所有设备的set
				jedis.sadd("allRedisSiteKey", redis_site_key);
			}
			log.info("共读入"+site_count+"个场所");

			//device 设备信息
			List<Device> devices = new ArrayList<Device>();
			try {
				db = new DBBridge();
				db.openBridge();
				log.info("读入设备信息");
				sql = "select * from tbl_device;";//device这张表需要读入的信息有
				ResultSet rs = db.execSELECT(sql);//执行sql语句
				while(rs.next()) {
					Device new_device = new Device();
					new_device.getFromResult(rs);
					devices.add(new_device);
				}
			} catch (Exception e) {
				log.error("数据库操作错误：",e);
			} finally {
				try {
					db.closeBridge();
				} catch (Exception e2) {
					log.error("异常：", e2);
				}
			}
			Iterator<Device> DeviceItor = devices.iterator();
			int device_count = 0;
			while(DeviceItor.hasNext()) {
				Device device = DeviceItor.next();
				String device_code = device.getDevice_code();
				if (device_code == null) {
					log.error("device_code cannot be null");
					continue;
				}
				String place_code = device.getPlace_code();
				if (place_code == null) {
					log.error("device:"+device_code+"have no place_code,place_code cannot be null");
					continue;
				}
				device_count++;
				String redis_device_key = KeyExchange.getRedisDeviceKeyFromDeviceCode(device_code);
				
				Map<String, String> map = new HashMap<String, String>();
				//读取设备基本信息
				map.put("DeviceCode", device_code);
				map.put("PlaceCode", place_code);
				map.put("AreaCode", device.getArea_code());

				String OnlineStatus = device.getService_online_status();
				String DataStatus = device.getData_online_status();
				String lastRefreshTime = device.getLast_online_time();
				String lastDataRefreshTime = device.getLast_data_time();
				String device_longitude = device.getDevice_longitude();
				String device_latitude = device.getDevice_latitude();
				//表定义中如果没有默认值,在这里赋初值
				if (OnlineStatus == null) {
					OnlineStatus = "0";
				}
				if (DataStatus == null) {
					DataStatus = "0";
				}
				if (lastRefreshTime == null) {
					lastRefreshTime = "0";
				}
				if (lastDataRefreshTime == null) {
					lastDataRefreshTime = "0";
				}
				if (device_longitude == null) {
					device_longitude = "0.000000";
				}
				if (device_latitude == null) {
					device_latitude = "0.000000";
				}
				map.put("OnlineStatus", OnlineStatus);
				map.put("DataStatus", DataStatus);
				map.put("lastRefreshTime", lastRefreshTime);
				map.put("lastDataRefreshTime", lastDataRefreshTime);
				map.put("DeviceLongitude", device_longitude);
				map.put("DeviceLatitude", device_latitude);
				jedis.hmset(redis_device_key, map);
				
				//将设备归入它所属的场所
				String redis_site_set_key = KeyExchange.getRedisSiteSetKeyFromDeviceCode(place_code);
				jedis.sadd(redis_site_set_key, device_code);
				
				//将设备加到一个装着所有设备的set
				jedis.sadd("allRedisDeviceKey", redis_device_key);
			}
			log.info("共读入"+device_count+"个设备");
		  } catch (Exception e) {
			  log.error("数据操作错误：",e);
		  } finally {
			  try {
					db.closeBridge();
				} catch (Exception e2) {
					log.error("异常：", e2);
				}
		  }
		  jedis.close();
	}
}
