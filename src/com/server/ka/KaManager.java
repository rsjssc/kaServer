package com.server.ka;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.database.DBBridge;
import com.util.PubPara;

public class KaManager 
{
	private static Logger  logger  = Logger.getLogger(KaManager.class);
	
	public String sendKa(String VerifyID,String cmdLine) 
	{
		//Cpu Mem	MemRate	Hd	HdRate	Errlog（redis中没有）	UpSpeed	DwnSpeed	RunTime	uploadMode	dataSrc	SrcType	DeviceVer	SysTime	WirelessPower
		String[] tokens = cmdLine.split(";");
		if (tokens.length != 15) {
			logger.info("cmdLine length is incomplete, expect have 15 parts, actually "+tokens.length+" parts.");
			return "cmdLine error";
		}
		//这里需不需要检查每一个字段的字段名是否符合，比如token[0]的等号左边是否为Cpu
		for (int i = 0; i < tokens.length; i++) {
			if (!tokens[i].contains("=")) {
				logger.info("cmdLine tokens: "+tokens[i]+" does not has \"=\"");
				return "cmdLine error";
			}
		}
		//
		Jedis jedis = new Jedis(PubPara.strRedisIP, PubPara.nRedisPort);
		String Cpu = tokens[0].split("=")[1];
		String Mem = tokens[1].split("=")[1];
		String MemRate = tokens[2].split("=")[1];
		String Hd = tokens[3].split("=")[1];
		String HdRate = tokens[4].split("=")[1];
		String UpSpeed = tokens[6].split("=")[1];
		String DwnSpeed = tokens[7].split("=")[1];
		String RunTime = tokens[8].split("=")[1];
		String UploadMode = tokens[9].split("=")[1];
		String DataSrc = tokens[10].split("=")[1];//设备编号
		String SrcType = tokens[11].split("=")[1];//场所类型不是场所编号？
		String DeviceVer = tokens[12].split("=")[1];
		String SysTime = tokens[13].split("=")[1];
		String WirelessPower = tokens[14].split("=")[1];
		String device_code = DataSrc;
		String redis_device_key = "status_device_"+device_code;
		String place_code = jedis.hget(redis_device_key, "PlaceCode");
		String redis_site_key = "status_site_" + place_code;
		
		//
		RedisManager rdsmanager = new RedisManager();
		if (!rdsmanager.checkRedisExist(jedis, redis_device_key)) {
			return "your device code is not exit in system";
		}
		//如果SrcType不是场所编号，则到redis中查找场所编号
//		String place_code = jedis.hget(device_code, "place_code");
		
//		log.info("\t it's device_code: " + device_code);
//		System.out.println("\t it's device_code: " + device_code);

		String time = Long.toString(System.currentTimeMillis()/1000);
		if (rdsmanager.checkRedisOffline(jedis,redis_device_key)) // refresh redis and mysql
		{
			logger.info("going to change the device_online_status on postgresql,with device code: " + device_code);
			DBBridge db = new DBBridge();
			try {
				db.openBridge();
				String strSql = "update tbl_device set service_online_status=1,last_online_time="+time	//device online_status
//						+ "data_online_status=1,last_data_time="+time+" "	//data online_status
								+ "where device_code='"+ device_code +"';";
				db.executeUpdate(strSql);
				
		  }catch (Exception e) {
				logger.error("数据操作错误",e);
			}finally
			{
				try {
					db.closeBridge();
				} catch (Exception e2) {
					logger.error("异常：", e2);
				}
			}
			
			//作为一个map一次更新到redis，但是不知道这样会不会将原来存在的配置语句删除
			Map<String,String> device_ka= new HashMap<String,String>();
			//ka stat
			device_ka.put("Cpu", Cpu);
			device_ka.put("Mem",Mem);
			device_ka.put("MemRate",MemRate);
			device_ka.put("Hd",Hd);
			device_ka.put("HdRate",HdRate);;
			device_ka.put("UpSpeed",UpSpeed);
			device_ka.put("DwnSpeed",DwnSpeed);
			device_ka.put("RunTime",RunTime);
			device_ka.put("UploadMode",UploadMode);
			device_ka.put("DeviceCode",DataSrc);//设备编号？
			device_ka.put("SrcType",SrcType);
			device_ka.put("DeviceVer",DeviceVer);
			device_ka.put("SysTime",SysTime);
			device_ka.put("WirelessPower",WirelessPower);
			
			//alive refresh
			device_ka.put("OnlineStatus","1");
			device_ka.put("lastRefreshTime",time);
			
			jedis.hmset(redis_device_key, device_ka);
			
			//场所最后更新时间
			jedis.hset(redis_site_key, "lastRefreshTime", time);
			
		} else {
			//refresh redis lastRefreshTime
			jedis.hset(redis_device_key, "lastRefreshTime", time);
			jedis.hset(redis_site_key, "lastRefreshTime", time);
		}
		String KaReturn = rdsmanager.getConf(jedis,redis_device_key);
		jedis.close();
		//log.info("going to return to verifyID: " + VerifyID+", this: "+KaReturn);
		return KaReturn;
	}
	
}
