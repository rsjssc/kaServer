package com.test;

import org.apache.log4j.Logger;

import com.database.DBBridge;

/** 
 * 概述： 
 * @author  zhangy 
 * @version testPg.java 2016年2月18日 下午2:48:14 
 */
public class testPg 
{
	private static Logger logger = Logger.getLogger(testPg.class);
	
	
	public int testPostgres()
	{
		logger.info("test test test pg pg pg ....");
		int nCount = 0;
		DBBridge db = new DBBridge();
		try {
			db.openBridge();
			String strSql = "select count(*) from gb_2260";
			db.execSELECT(strSql);
			if(db.nextRow())
			{
				nCount = db.getInt("count");
				logger.info(nCount);
			}
			
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
		return nCount;
	}
	
	
	public static void main(String[] args) {
		
//		logger.info("test test test pg pg pg ....");
//		DBBridge db = new DBBridge();
//		try {
//			db.openBridge();
//			String strSql = "select count(*) from gb_2260";
//			db.executeUpdate(strSql);
//			if(db.nextRow())
//			{
//				logger.info(db.getInt("count"));
//			}
//			
//	  }catch (Exception e) {
//			logger.error("数据操作错误",e);
//		}finally
//		{
//			try {
//				db.closeBridge();
//			} catch (Exception e2) {
//				logger.error("异常：", e2);
//			}
//		}
		
	}

}
