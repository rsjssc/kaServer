package com.server.modules;

import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.database.DBBridge;
import com.util.PubPara;

import redis.clients.jedis.Jedis;

public class Monitor extends Thread {
	private static Logger log = Logger.getLogger(Monitor.class);

	@Override
    public void run() {
    	log.info("monitor thread start now!");
    	while (true) {
    		long start = System.currentTimeMillis()/1000;
        	int modifySiteOnlineCount = 0;
        	int modifyDeviceOnlineCount = 0;
        	
        	Jedis jedis = null;
    		String sql = null;
    		
    		DBBridge db = new DBBridge();
    		try {
    			db.openBridge();
        		jedis = new Jedis(PubPara.strRedisIP,PubPara.nRedisPort);
        		
        		long cuurentTime = (System.currentTimeMillis()/1000);
        		
        		Iterator<String> iter = jedis.smembers("allRedisSiteKey").iterator();//获取所有的场所
//        		log.info("read from redis time epside is :"+(System.currentTimeMillis()-cuurentTime)+"s.");
        		while (iter.hasNext()) {
        			String redis_site_key = iter.next();
        			String place_code = KeyExchange.getDeviceCodeFromRedisDeviceKey(redis_site_key);
        			String redis_site_set_key = KeyExchange.getRedisSiteSetKeyFromDeviceCode(place_code);
//        			log.info("redis place code:"+redis_site_key);
//        			log.info("place_code:"+place_code);
//        			log.info("redis_site_set_key:"+redis_site_set_key);
        			int site_online_status = Integer.parseInt(jedis.hget(redis_site_key, "OnlineStatus"));
        			long site_lastRefreshTime = Long.parseLong(jedis.hget(redis_site_key, "lastRefreshTime"));
//        			log.info("site"+place_code+"'s online_status is "+site_online_status);
        			int isAllDevicesOnline = 1;
        			
        			Set<String> devices = jedis.smembers(redis_site_set_key);
        			if (devices.isEmpty()) {//没有设备的场所是没有意义的场所
        				log.error("place: \"" + place_code + "\" is contain no device.pleas add device or delete place.");
        				try {
        					sql="update tbl_site set service_online_status=0 where place_code='"+ place_code +"';";
        					db.executeUpdate(sql);
        					jedis.hset(redis_site_key, "OnlineStatus", "0");
        				} catch (Exception e) {
        					log.error("数据操作错误",e);
        				}
        				continue;
        			}
        			for (String dev : devices) {
//        				log.info("check device :"+dev);
        				String redis_device_key = KeyExchange.getRedisDeviceKeyFromDeviceCode(dev);
        				int device_online_status = Integer.parseInt(jedis.hget(redis_device_key, "OnlineStatus"));
        				//更新对应场所的最后更新时间
        				long device_lastRefreshTime = Long.parseLong(jedis.hget(redis_device_key, "lastRefreshTime"));
        				if (site_lastRefreshTime < device_lastRefreshTime) {
        					jedis.hset(redis_site_key, "lastRefreshTime", Long.toString(device_lastRefreshTime));
        				}
        				//check is the dev's  OnlineStatus overtime
        				if (device_online_status == 1) {
        					long lastRefreshTime = Long.parseLong(jedis.hget(redis_device_key, "lastRefreshTime"));
//        					log.info(cuurentTime - lastRefreshTime);
        					if (cuurentTime - lastRefreshTime > 180) {//180s
        						device_online_status = 0;
        						sql="update tbl_device set service_online_status=0,last_online_time="+lastRefreshTime+" where device_code='"+ dev +"';";
        						try {
        							db.executeUpdate(sql);
        							jedis.hset(redis_device_key, "OnlineStatus", "0");
        							modifyDeviceOnlineCount++;
        						} catch (Exception e) {
        							log.error("数据操作错误",e);
        						}
        					}
        				}
        				isAllDevicesOnline &= device_online_status;
        				
        			}
//        			log.info("site"+place_code+" is AllDevicesOnline: "+ isAllDevicesOnline);
        			//change the online status of site
        			if (site_online_status == 1) {
        				if (isAllDevicesOnline == 0) {
        					sql="update tbl_site set service_online_status=0,end_online_time="+site_lastRefreshTime+" where place_code='"+ place_code +"';";
        					try {
        						db.executeUpdate(sql);
//        						stmt.addBatch("update tbl_site set service_online_status=0,end_online_time="+site_lastRefreshTime+" where place_code='"+ place_code +"';");
        						jedis.hset(redis_site_key, "OnlineStatus", "0");
        						modifySiteOnlineCount++;
        					} catch (Exception e) {
        						log.error("数据操作错误",e);
        					}
        				}
        			} else {
        				if (isAllDevicesOnline == 1) {
        					//set student_online_status=1 in mysql And let's data in redis sync to mysql
        					
        					sql="update tbl_site set service_online_status=1,end_online_time="+site_lastRefreshTime+" where place_code='"+ place_code +"';";
        					try {
        						db.executeUpdate(sql);
//        						stmt.addBatch("update tbl_site set service_online_status=1,end_online_time="+site_lastRefreshTime+" where place_code='"+ place_code +"';");
        						jedis.hset(redis_site_key, "OnlineStatus", "1");
        						modifySiteOnlineCount++;
        					} catch (Exception e) {
        						log.error("数据操作错误",e);
        					}
        				}
        			}
        		}
        		log.info("modify Site Online Count:"+modifySiteOnlineCount);
       		  	log.info("modify device Online Count:"+modifyDeviceOnlineCount);
       		  	log.info("this round monitor time epside is :"+(System.currentTimeMillis()/1000-start)+"s.");
			} catch (Exception e) {
				log.error("异常：",e);
			} finally {
				 try {
						db.closeBridge();
						jedis.close();
					} catch (Exception e2) {
						log.error("异常：", e2);
					}
			}
    		
    		//ka检测模块周期
    		try {
				sleep(1000*180);
			} catch (InterruptedException e) {
				log.error(e);
			}
    		
    	}
    }
}