package com.system;

import java.io.File;

import org.apache.log4j.Logger;

import com.util.PubFunc;
import com.util.PubString;

public class ClearDataThread extends Thread
{
	private static Logger  logger  = Logger.getLogger(ClearDataThread.class);
	
	public void run()
	{
		mkAndClearDir();
		while (true)
		{
			logger.info("ClearDataThread start ...");
			mkAndClearDir();
			try
			{
				Thread.sleep(1000 * 60 * 60 * 12);
			}catch (Exception e)
			{
				logger.error(e);
			}
		}
	}
	
	
	/**
	 * 删除一些临时文件 zhangy 20101210
	 */
	private void mkAndClearDir()
	{
		try {
			String strTodayDate = PubFunc.LongTime2FormatStr(PubFunc.getServerMS(), "yyyy-MM-dd");
			String strYesterdayDate = PubFunc.LongTime2FormatStr((PubFunc.getServerMS() - 1000 * 60 * 60 * 24), "yyyy-MM-dd");
			//
			File backTodayDir = new File(PubString.strBackDataPath + strTodayDate);
			if ((!backTodayDir.exists()) || (!backTodayDir.isDirectory())) {
				backTodayDir.mkdir();
				logger.info("mkdir:" + PubString.strBackDataPath + strTodayDate);
			}
			File errorTodayDir = new File(PubString.strErrorDataPath + strTodayDate);
			if ((!errorTodayDir.exists()) || (!errorTodayDir.isDirectory())) {
				errorTodayDir.mkdir();
				logger.info("mkdir:" + PubString.strErrorDataPath + strTodayDate);
			}
			
			// 清除最近两条之前的目录，保存两天的数据
			deleteOldDir(PubString.strBackDataPath, strTodayDate, strYesterdayDate);
			
			deleteOldDir(PubString.strErrorDataPath, strTodayDate, strYesterdayDate);
			
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	
	/**
	 * 删除子目录
	 */
	private void deleteOldDir(String strParentPath, String strTodayDate, String strYesterdayDate)
	{
		File backfiles = new File(strParentPath);
		File[] astrBackFile = backfiles.listFiles();
		for (int i = 0; i < astrBackFile.length; i++)
		{
			if (astrBackFile[i].isDirectory())
			{
				if(!strTodayDate.equalsIgnoreCase(astrBackFile[i].getName()) && !strYesterdayDate.equalsIgnoreCase(astrBackFile[i].getName()))
				{
					astrBackFile[i].delete();
				}
			}
		}
	}
}
