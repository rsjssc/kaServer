package com.util;

import java.util.ArrayList;
import java.util.List;

/** 
 * 概述： 
 * @author  zhangy 
 * @version PubString.java 2016年2月19日 下午3:43:03 
 */
public class PubString 
{
	
	/**
	 * 设备上传文件目录
	 */
	public final static String		SFTP_DATA_PATH	= "/home/userSftp/qzt_sftp/data/";
	
	/**
	 * 处理数据目录
	 */
	public static String strUnzipPath	= "/home/data/unzipData/";
	public static String strTempPath		= "/home/data/tempData/";
	public static String strHdfsPath	= "/home/data/hdfsData/";
	public static String strErrorDataPath	= "/home/data/errorData/";
	public static String strBackDataPath	= "/home/data/backData/";
	
	
	/**
	 * 
	 */
	public static List<String> bcpFilesList		= new ArrayList<String>(); //存放解压后bcp文件

}
