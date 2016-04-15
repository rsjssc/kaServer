package com.server.modules;

import java.util.Iterator;
import org.apache.log4j.Logger;
import com.database.DBBridge;
import com.util.PubPara;

import redis.clients.jedis.Jedis;
import sun.nio.ch.sctp.Shutdown;

/**
 * webserver程序在退出时将redis中的ka在线数据同步到postgresql
 * ftp周期性检查程序写到redis中的数据在线状态不由本部分负责，即不负责将数据在线情况同步到数据库
 * @author kit
 *
 */
public class shutdown {
	private static Logger log = Logger.getLogger(Shutdown.class);
	
	/**
	 * 程序在退出时执行将当前redis中ka在线状态的信息更新到数据库
	 */
	public void kaMonitorShutdownWork(){
		
		String sql = null;
		Jedis jedis = null;
		DBBridge db = new DBBridge();
		try {
			db.openBridge();
    		jedis = new Jedis(PubPara.strRedisIP,PubPara.nRedisPort);
    		
    		Iterator<String> iter;
    		//同步设备ka在线信息
    		iter = jedis.smembers("allRedisDeviceKey").iterator();//获取所有设备
    		while (iter.hasNext()) {
    			String redis_device_key = iter.next();
    			try {
    				int device_online_status = Integer.parseInt(jedis.hget(redis_device_key, "OnlineStatus"));
    				long lastRefreshTime = Long.parseLong(jedis.hget(redis_device_key, "lastRefreshTime"));
    				String device_code = KeyExchange.getDeviceCodeFromRedisDeviceKey(redis_device_key);
    				sql="update tbl_device set service_online_status="+device_online_status+",last_online_time="+lastRefreshTime+" where device_code='"+ device_code +"';";
    				db.executeUpdate(sql);
    			} catch (Exception e) {
    				log.error("数据操作错误",e);
    			}
    		}
    		
    		//同步场所ka在线部分
    		iter = jedis.smembers("allRedisSiteKey").iterator();//获取所有场所
    		while (iter.hasNext()) {
    			String redis_site_key = iter.next();
    			try {
    				int site_online_status = Integer.parseInt(jedis.hget(redis_site_key, "OnlineStatus"));
    				long site_lastRefreshTime = Long.parseLong(jedis.hget(redis_site_key, "lastRefreshTime"));
    				String place_code = KeyExchange.getDeviceCodeFromRedisDeviceKey(redis_site_key);
    				sql="update tbl_site set service_online_status="+site_online_status+",end_online_time="+site_lastRefreshTime+" where place_code='"+ place_code +"';";
    				db.executeUpdate(sql);
    			} catch (Exception e) {
    				log.error("数据操作错误",e);
    			}
    		}
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
		
		log.info("ka Monitor going to shutdown.have do some sync to postgresql");
	}
}
