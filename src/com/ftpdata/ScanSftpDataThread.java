package com.ftpdata;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.database.DBBridge;
import com.server.modules.KeyExchange;
import com.util.PubFunc;
import com.util.PubPara;
import com.util.PubString;
import com.util.ZipUtil;

import redis.clients.jedis.Jedis;

/** 
 * 概述： 
 * @author  zhangy 
 * @version ScanSftpDataThread.java 2016年2月19日 下午4:14:22 
 */
public class ScanSftpDataThread extends Thread {
	
	private static Logger  logger  = Logger.getLogger(ScanSftpDataThread.class);
	private HashMap<String, String> deviceCodeHash = new HashMap<String, String>();
	
	public void run() 
	{
			logger.info("ScanSftpDataThread start ...");
			while(true)
			{
				try 
				{
						try {
							String [] astrFiles = getFiles(PubString.SFTP_DATA_PATH);
							if(astrFiles != null){
								if(astrFiles.length>0){
									logger.info("scan :: "  + PubString.SFTP_DATA_PATH + " ::: " + astrFiles.length);
									DealFiles(astrFiles, PubString.SFTP_DATA_PATH);
									//更新redis中的数据状态
									updateRedisDataStatus();
								}
							}
						} catch (Exception e) {
							logger.error("error ",e);
						}
						
				} catch (Exception e) {
					logger.error("error: ",e);
				}
				//一分钟一次
				try {
					sleep(1000 * 60);
				} catch (InterruptedException e) {
					logger.error(e);
				}
			}
	}
	
	/**
	 * @param strScrPath
	 * @return
	 */
	public String[] getFiles(String strScrPath) {
		File files = new File(strScrPath);
		String[] astrFile=files.list();
		return astrFile;
	}
	
	/**
	 * @param strFiles
	 * @param strPath
	 * @param strCustomerNoCode
	 */
	public void DealFiles(String[] strFiles,String strPath) {
		logger.info(strPath + " files length:" + strFiles.length);
		for(String strName:strFiles){
			try 
			{
				if(strName.endsWith("_ok"))
				{
					String strDeviceCode = strName.split("_")[0];
					deviceCodeHash.put(strDeviceCode, ""+(PubFunc.getServerMS()/1000));	//这里每次都运行一次获取时间吗
					String strZipFileName = strName.split("_")[1];
					MoveToEachPlace(strPath, strZipFileName);
					//删除标志文件
					new File(strPath+strName).delete();
					
				}else 
				{
					PubFunc.execSysCmd("mv " + strPath + strName + " " + PubFunc.getSonDir(PubString.strErrorDataPath) );
				}
				
			} catch (Exception e) {
				logger.error("DealFiles异常:" + e);
			}
		}
	}
	
	
	
	/**
	 * @param strSrc
	 * @param strFileName
	 * @param getCustomerName
	 */
	public static void MoveToEachPlace(String strSrc, String strFileName) 
	{
		try {
			//zip文件解压到unzipData目录
			logger.info(strSrc+strFileName + " :::to::: " + PubString.strUnzipPath);
			File[] astrFile = ZipUtil.unzip(strSrc+strFileName, PubString.strUnzipPath, "");
			for (File file : astrFile) 
			{
				if (file.getName().endsWith(".xml"))
				{
					file.delete();
				}else if(file.getName().endsWith(".bcp"))
				{
					PubString.bcpFilesList.add(file.toString());
				}
			}
			//备份zip文件（临时）
			PubFunc.execSysCmd("mv " + strSrc+strFileName + " " + PubFunc.getSonDir(PubString.strBackDataPath));
			
		} catch (Exception e) 
		{
			logger.error("异常：",e);
			PubFunc.execSysCmd("mv " + strSrc+strFileName + " " + PubFunc.getSonDir(PubString.strErrorDataPath));
		}
	}
	
	
	
	/**
	 * 更新Redis中设备和场所的数据状态
	 * 将deviceCodeHash中的设备的数据在线状态置为在线，并且更新数据在线状态时间last_data_time
	 * 
	 * ruansj
	 * 这里只负责把设备在redis中的数据状态从离线变成在线，其他功能在SftpMonitor.java
	 */
	public void updateRedisDataStatus()
	{
		int modifyDeviceDataOnlineCount = 0;
		long start = System.currentTimeMillis()/1000;
		Iterator<String> iter = deviceCodeHash.keySet().iterator();
		Jedis jedis = null;
		String sql= null;
		DBBridge db = new DBBridge();
		try {
			jedis = new Jedis(PubPara.strRedisIP,PubPara.nRedisPort);
		} catch (Exception e) {
			logger.error("redis connection wrong",e);
		}
		
		try 
		{
			db.openBridge();
			while (iter.hasNext()) {
				String device_code = iter.next();
				String redis_device_key = KeyExchange.getRedisDeviceKeyFromDeviceCode(device_code);
				String lastDataRefreshTime = deviceCodeHash.get(device_code).toString();
				int device_data_status = Integer.parseInt(jedis.hget(redis_device_key, "DataStatus"));
				if (device_data_status == 0) {
					try {
						sql="update tbl_device set data_online_status=1,last_data_time="+lastDataRefreshTime+" where device_code='"+ device_code +"';";
						db.executeUpdate(sql);
						jedis.hset(redis_device_key, "DataStatus", "1");
						jedis.hset(redis_device_key, "lastDataRefreshTime", lastDataRefreshTime);
					} catch (Exception e) {
						logger.error("data operation wrong",e);
					}
				} else if (device_data_status == 1){
					try {
						jedis.hset(redis_device_key, "lastDataRefreshTime", lastDataRefreshTime);
					} catch (Exception e) {
						logger.error("redis operation wrong",e);
					}
				} else {
					logger.error("device: "+device_code+" ,device_data_status="+device_data_status+". it must be 0 or 1.");
					continue;
				}
				modifyDeviceDataOnlineCount++;
			}
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
		logger.info("modify device Data Online Count:"+modifyDeviceDataOnlineCount);
		logger.info("updateRedisDataStatus time epside is :"+(System.currentTimeMillis()/1000-start)+"s.");
		if (jedis != null) {
			try {
				jedis.close();
			} catch (Exception e) {
				logger.error("redis close wrong",e);
			}
		}
		
		this.deviceCodeHash.clear();
	}
	
}
