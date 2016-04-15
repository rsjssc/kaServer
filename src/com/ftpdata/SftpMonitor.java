package com.ftpdata;

import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.database.DBBridge;
import com.server.modules.KeyExchange;
import com.util.PubPara;
import com.util.PubString;

import redis.clients.jedis.Jedis;

/**
 * 数据在线周期性检查模块
 * 负责把设备的状态从在线置为离线，场所状态的双向转换
 * @author ruansj
 *
 */
public class SftpMonitor  extends Thread {
	private static Logger  logger  = Logger.getLogger(SftpMonitor.class);
	
	public void run() 
	{
		int offLinePeriod_data = 60*60;
		logger.info("SftpMonitor start ...,data offLinePeriod is:"+offLinePeriod_data);
		while(true)
		{
			String sql = null;
			Jedis jedis = null;
			try {
				jedis = new Jedis(PubPara.strRedisIP,PubPara.nRedisPort);
			} catch (Exception e) {
				logger.error("redis connection wrong",e);
				continue;
			}
			DBBridge db = new DBBridge();
			
			try 
			{
				db.openBridge();
				long start = System.currentTimeMillis()/1000;
		    	int modifySiteDataOnlineCount = 0;
		    	int modifyDeviceDataOnlineCount = 0;
				//设备数据在线部分
				//遍历redis中所有的设备，如果在一定时间范围内还没有数据上传的设备认为是数据离线，对于数据离线的设备，同时修改redis和postgresql中的项
		    	Iterator<String> iter = jedis.smembers("allRedisDeviceKey").iterator();//获取所有设备
				while (iter.hasNext()) {
					String redis_device_key = iter.next();
					//check is the dev's  DataStatus overtime
					int device_data_status = Integer.parseInt(jedis.hget(redis_device_key, "DataStatus"));
					if (device_data_status == 1) {
						long lastDataRefreshTime = Long.parseLong(jedis.hget(redis_device_key, "lastDataRefreshTime"));
						String device_code = KeyExchange.getDeviceCodeFromRedisDeviceKey(redis_device_key);
						if (start - lastDataRefreshTime > offLinePeriod_data) {
							device_data_status = 0;
							try {
								sql="update tbl_device set data_online_status=0,last_data_time="+lastDataRefreshTime+" where device_code='"+ device_code +"';";
								db.executeUpdate(sql);
								jedis.hset(redis_device_key, "DataStatus", "0");
//								logger.info("modify device:"+device_code+"'s datastatus to offline");
								modifyDeviceDataOnlineCount++;
							} catch (Exception e) {
								logger.error("数据操作错误",e);
							}
						} else {//update last_data_time in postgresql
							try {
								sql="update tbl_device set last_data_time="+lastDataRefreshTime+" where device_code='"+ device_code +"';";
								db.executeUpdate(sql);
//								logger.info("modify device:"+device_code+"'s datastatus to offline");
								modifyDeviceDataOnlineCount++;
							} catch (Exception e) {
								logger.error("数据操作错误",e);
							}
						}
					}
				}
				
				//场所数据在线部分
				//遍历redis中所有的场所，场所所属的设备都数据在线，则场所在线，否则场所离线，如果场所数据在线状态发生变化则同步更新redis和postgresql中的项
				iter = jedis.smembers("allRedisSiteKey").iterator();//获取所有场所
				while (iter.hasNext()) {
					String redis_site_key = iter.next();
					int site_data_online_status = Integer.parseInt(jedis.hget(redis_site_key, "DataStatus"));
					Long site_lastDataRefreshTime = Long.parseLong(jedis.hget(redis_site_key, "lastDataRefreshTime"));
					int isAllDevicesDataOnline = 1;
					
					String place_code = KeyExchange.getSiteCodeFromRedisSiteKey(redis_site_key);
					String redis_rel_key = KeyExchange.getRedisSiteSetKeyFromDeviceCode(place_code);
					
					//查看场所的所有设备是否都在线
					Set<String> devices = jedis.smembers(redis_rel_key);
					if (devices.isEmpty()) {//没有设备的场所是没有意义的场所
						logger.error("place: \"" + place_code + "\" is contain no device.pleas add device or delete place.");
						try {
							sql="update tbl_site set data_online_status=0 where place_code='"+ place_code +"';";
							db.executeUpdate(sql);
							jedis.hset(redis_site_key, "DataStatus", "0");
						} catch (Exception e) {
							logger.error("数据操作错误",e);
						}
						continue;
					}
					Iterator<String> iterForDevices = devices.iterator();
					while (iterForDevices.hasNext()) {
						String device_code = iterForDevices.next();
						String redis_device_key = KeyExchange.getRedisDeviceKeyFromDeviceCode(device_code);
						int device_data_status = Integer.parseInt(jedis.hget(redis_device_key, "DataStatus"));
						isAllDevicesDataOnline &= device_data_status;
						Long device_lastDataRefreshTime = Long.parseLong(jedis.hget(redis_device_key, "lastDataRefreshTime"));
						//更新场所最后数据更新时间
						if (device_lastDataRefreshTime > site_lastDataRefreshTime) {
							site_lastDataRefreshTime = device_lastDataRefreshTime;
						}
					}
					jedis.hset(redis_site_key, "lastDataRefreshTime", site_lastDataRefreshTime.toString());
					//change the data online status of site
					if (site_data_online_status == 1) {
						if (isAllDevicesDataOnline == 0) { //原来在线，变成离线，需要同时修改redis和postgresql
							try {
								sql="update tbl_site set data_online_status=0,last_data_time="+site_lastDataRefreshTime+" where place_code='"+ place_code +"';";
								db.executeUpdate(sql);
								jedis.hset(redis_site_key, "DataStatus", "0");
								modifySiteDataOnlineCount++;
							} catch (Exception e) {
								logger.error("数据操作错误",e);
							}
						}
					} else {
						if (isAllDevicesDataOnline == 1) {//原来不在线，现在变成在线，需要同时修改redis和postgresql
							try {
								sql="update tbl_site set data_online_status=1,last_data_time="+site_lastDataRefreshTime+" where place_code='"+ place_code +"';";
								db.executeUpdate(sql);
								jedis.hset(redis_site_key, "DataStatus", "1");
								modifySiteDataOnlineCount++;
							} catch (Exception e) {
								logger.error("数据操作错误",e);
							}
						}
					}
				}
				
				logger.info("modify Site Data Online Count:"+modifySiteDataOnlineCount);
				logger.info("modify device Data Online Count:"+modifyDeviceDataOnlineCount);
				logger.info("this round ftp_monitor time epside is :"+(System.currentTimeMillis()-start)/1000+"s.");
			} catch (Exception e) {
				logger.error("error: ",e);
			} finally {
				 try {
						db.closeBridge();
						jedis.close();
					} catch (Exception e2) {
						logger.error("异常：", e2);
					}
			}
			
			if (jedis != null) {
				try {
					jedis.close();
				} catch (Exception e) {
					logger.error("redis close wrong",e);
				}
			}
			//十分钟一次，可以在配置文件中增加这一周期的选项
			try {
				sleep(1000 * 600);
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
	}
}
