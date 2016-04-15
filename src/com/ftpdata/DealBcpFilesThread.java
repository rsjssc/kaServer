package com.ftpdata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.util.PubFunc;
import com.util.PubString;

/** 
 * 概述： 
 * @author  zhangy 
 * @version DealBcpFilesThread.java 2016年2月19日 下午5:06:13 
 */
public class DealBcpFilesThread extends Thread {
	
	private static Logger  logger  = Logger.getLogger(DealBcpFilesThread.class);
	private List<String> totalFileList = new ArrayList<String>();
	private List<String> tmacFileList = new ArrayList<String>();
	private List<String> hotspotFileList = new ArrayList<String>();
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run ()
	{
		try {
			sleep(1000 * 10);
		} catch (Exception e) {
			logger.error(e);
		} 
		
		while(true)
		{
			try {
				
				if (PubString.bcpFilesList.size()>0) {
					logger.info(" DealBcpFilesThread length ::: " + PubString.bcpFilesList.size());
					totalFileList = PubString.bcpFilesList;
					splitBcpFiles(totalFileList);
					logger.info(" afterafterafterafterafter11111 ::: " + totalFileList.size());
					logger.info(" afterafterafterafterafter22222 ::: " + PubString.bcpFilesList.size());
				}
				
			} catch (Exception e) {
				logger.error("error",e);
			}
			try {
				Thread.sleep(1000 * 60);
			} catch (InterruptedException e) {
				logger.error("error",e);
			}
		}
	}
	
	/**
	 * @param strType
	 * @param listLog
	 */
	private void splitBcpFiles(List<String> fileList)
	{
		if (fileList.size() > 0)
		{
			for (String bcpFile : fileList) 
			{
				File file = new File(bcpFile);
				if(file.getName().indexOf("WA_SOURCE_FJ_1001")!=-1) //终端MAC信息
				{
					tmacFileList.add(bcpFile);
				}else if(file.getName().indexOf("WA_SOURCE_FJ_1002")!=-1) //采集热点信息
				{
					hotspotFileList.add(bcpFile);
				}
			}
			logger.info("tmacFileList 111111 ::: " + tmacFileList.size());
			logger.info("hotspotFileList 111111 ::: " + hotspotFileList.size());
			//
			combineBcpFiles("tmac",tmacFileList);
			combineBcpFiles("hotspot",hotspotFileList);
			fileList.clear();
			logger.info("tmacFileList 222222 ::: " + tmacFileList.size());
			logger.info("hotspotFileList 222222 ::: " + hotspotFileList.size());
		}
	}
	
	/**
	 * 合并文件
	 * @param strType
	 * @param listWlanLog
	 */
	private void combineBcpFiles(String strType, List<String> listLog)
	{
		logger.info(strType + "文件合并总数:" + listLog.size());
		if (listLog.size() > 0)
		{
			String strCombineFile = PubString.strTempPath + strType + "_" + (PubFunc.getServerMS()/1000) + "_" + PubFunc.getRandCode(6);
			if (strCombineFile.equals(PubFunc.FileCombine(listLog, strCombineFile)))
			{
				PubFunc.execSysCmd("mv " + strCombineFile + " " + PubString.strHdfsPath);
				while (!listLog.isEmpty())
				{
					String strFile = listLog.get(0);
					File file = new File(strFile);
					if (file.exists())
					{
						file.delete();
					}
					listLog.remove(0);
				}
			}
		}
	}
	
}
