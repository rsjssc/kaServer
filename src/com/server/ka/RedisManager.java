package com.server.ka;

import org.apache.log4j.Logger;

import com.util.PubPara;

import redis.clients.jedis.Jedis;

/** 
 * 概述： 
 * @author  zhangy 
 * @version RedisManager.java 2016年2月18日 下午2:01:58 
 */
public class RedisManager 
{
	private static Logger  logger  = Logger.getLogger(RedisManager.class);
	
	public boolean checkRedisExist(Jedis jedis, String redis_device_key){
		if (jedis.hexists(redis_device_key, "OnlineStatus")) {
			return true;
		} else {
			logger.error("device_code:"+redis_device_key.substring(14)+" is not exit in redis");
			return false;
		}
	}
	
	/**
	 * check if data in redis is online,ture is offline which means need change in mysql,false is online,which means only need to refresh lastUploadTime
	 * @param studentId
	 * @return
	 */
	public boolean checkRedisOffline(Jedis jedis, String redis_device_key) {
		try {
			String status = jedis.hget(redis_device_key, "OnlineStatus");
			int stauts_int = Integer.parseInt(status);
			if (stauts_int == 0) {
				return true;
			}
		} catch (Exception e) {
			
			logger.error("redis操作错误",e);
		}
		return false;
		
	}
	
	public String getConf(Jedis jedis, String redis_device_key) {
		String Conf = "KaTime=" + PubPara.nKaTime + ";" 
	+ "KaAddress=" + PubPara.strKaUrl + ";"
	+ "FtpAddress=" + PubPara.strSftpUrl + ";"
	+ "FtpPort=" + PubPara.strSftpPort + ";"
	+ "FtpAccount=" + PubPara.strSftpAccount + ";"
	+ "FtpPasswd=" + PubPara.strSftpPassword + ";"
	+ "FtpTime=" + PubPara.nSftpTime + ";"
	+ "WirelessPower=" + jedis.hget(redis_device_key, "WirelessPower") + ";"//目前无法提供
	+ "AreaCode=" + jedis.hget(redis_device_key, "AreaCode") + ";"//目前无法提供
	+ "DeviceCode=" + jedis.hget(redis_device_key, "DeviceCode") + ";"
	+ "PlaceCode=" + jedis.hget(redis_device_key, "PlaceCode") + ";"
	+ "EqpLongitude=" + jedis.hget(redis_device_key, "DeviceLongitude") + ";"
	+ "EqpLatitude=" + jedis.hget(redis_device_key, "DeviceLatitude") + ";"
	+ "EupdateAddress=" + PubPara.strEupdateUrl + ";"
	+ "EupdatePort=" + PubPara.strEupdatePort + ";"
	+ "EupdateProtocol=" + PubPara.strEupdateProtocol + ";"
	+ "EupdateAccount=" + PubPara.strEupdateAccount + ";"
	+ "EupdatePasswd=" + PubPara.strEupdatePassword;
		return Conf;
	}
}
