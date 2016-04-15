/**
 * 内容： 作者：Administrator Liuxw 版本：1.0 修订： 2007-1-25 创建文件 备注：
 */
package com.database;

import java.util.HashMap;

/**
 * 概述：
 * 
 * @author KGZT
 * @version DBPara.java 2007-1-25 11:01:28
 */
public class DBPara
{

	public static int			DB_MAX_CONN			= 15;
	public static String		DB_DRIVER_NAME		= "org.postgresql.Driver";
	public static String		DB_URL				= "127.0.0.1";
	public static String		DB_BASENAME			= "beap";
	public static int			DB_PORT				= 5432;
	public static String		DB_USERNAME			= "postgres";
	public static String		DB_PASS				= "123456";
	private static volatile int	nLinkCount			= 0;						// 连接数量
	@SuppressWarnings("rawtypes")
	public static HashMap		hashTblStructure	= new HashMap();			// 表结构
	@SuppressWarnings("rawtypes")
	public static HashMap		hashTblColumn		= new HashMap();			// 表字段
	

	public static int getnLinkCount()
	{
		return nLinkCount;
	}

	public static void setnLinkCount(int nLinkCount)
	{
		DBPara.nLinkCount = nLinkCount;
	}

	public static synchronized int linkCountUp()
	{
		setnLinkCount(getnLinkCount() + 1);
		return getnLinkCount();
	}

	public static synchronized int linkCountDown()
	{
		setnLinkCount(getnLinkCount() - 1);
		return getnLinkCount();
	}

	/**
	 * @param db_max_conn
	 *            The dB_MAX_CONN to set.
	 */
	public static void setDB_MAX_CONN(int db_max_conn)
	{
		DB_MAX_CONN = db_max_conn;
	}

	//
	/**
	 * @param db_basename
	 *            The dB_BASENAME to set.
	 */
	public static void setDB_BASENAME(String db_basename)
	{
		DB_BASENAME = db_basename;
	}

	/**
	 * @param db_driver_name
	 *            The dB_DRIVER_NAME to set.
	 */
	public static void setDB_DRIVER_NAME(String db_driver_name)
	{
		DB_DRIVER_NAME = db_driver_name;
	}

	/**
	 * @param db_pass
	 *            The dB_PASS to set.
	 */
	public static void setDB_PASS(String db_pass)
	{
		DB_PASS = db_pass;
	}

	/**
	 * @param db_port
	 *            The dB_PORT to set.
	 */
	public static void setDB_PORT(int db_port)
	{
		DB_PORT = db_port;
	}

	/**
	 * @param db_url
	 *            The dB_URL to set.
	 */
	public static void setDB_URL(String db_url)
	{
		DB_URL = db_url;
	}

	/**
	 * @param db_username
	 *            The dB_USERNAME to set.
	 */
	public static void setDB_USERNAME(String db_username)
	{
		DB_USERNAME = db_username;
	}
}
