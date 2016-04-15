package com.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;



public class PubFunc 
{
	private static Logger logger = Logger.getLogger(PubFunc.class);
	public static String	ROOT_PATH	= "";
	public static String	PATH_PREFIX	= "/";
	public static String	DATA_CONFIG_PATH	= "";
	public static int 		ACTION_TYPE = 0; //操作行为类型 1：增加/修改  2：删除    3：查看
	
	/**
	 * 设置ROOT的起始路径
	 */
	public static void setRootPath(String rootpath)
	{
		ROOT_PATH = rootpath;
		PATH_PREFIX = (ROOT_PATH.indexOf("/") == -1 ? "\\" : "/");// Windows和Unix下的分隔符号是不同的
	}
	

	/**
	 * 设置DATA下配置文件的起始路径
	 */
	public static void setDATA_CONFIG_PATH(String data_config_path) 
	{
		DATA_CONFIG_PATH = data_config_path;
		PATH_PREFIX = (DATA_CONFIG_PATH.indexOf("/") == -1 ? "\\" : "/");// Windows和Unix下的分隔符号是不同的
	}
	

	/**
	 * 执行系统命令,返回命令结果字符串
	 * 
	 * @param strCmd
	 * @return
	 */
	public static String execSysCmd(String strCmd)
	{
		logger.info("execute system command:" + strCmd);
		String[] aStrCmd = { "/bin/sh", "-c", strCmd };
		StringBuffer sb = new StringBuffer();
		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		BufferedInputStream bis = null;
		BufferedReader br = null;
		try
		{
			process = runtime.exec(aStrCmd);
			bis = new BufferedInputStream(process.getInputStream());
			br = new BufferedReader(new InputStreamReader(bis));
			String strLine;
			while (null != (strLine = br.readLine()))
			{
				sb.append(strLine).append("\n");
			}
			if (process.waitFor() != 0)
			{
				process.destroy();
				logger.info("system cmd error:" + strCmd);
			}
			bis.close();
			br.close();
		}catch (Exception e)
		{
			logger.error("异常:", e);
		}finally
		{
			try {
				bis.close();
				br.close();
			} catch (IOException ex) {
				System.out.println("异常ex:" + ex);
			}
			//
			if(process!=null) process.destroy();
		}
		return sb.toString();
	}

	/**
	 * 去掉开始或结尾的字符
	 * 
	 * @param strInput
	 * @param strKey
	 * @return
	 */
	public static String trimEnd(String strInput, String strKey)
	{
		if (!isNull(strInput) && !isNull(strKey))
		{
			while (strInput.endsWith(strKey))// 去掉结尾的0
			{
				strInput = strInput.substring(0, strInput.length() - strKey.length());
			}
		}
		return strInput;
	}

	public static String trimBegin(String strInput, String strKey)
	{
		if (!isNull(strInput) && !isNull(strKey))
		{
			while (strInput.startsWith(strKey))// 去掉结尾的0
			{
				strInput = strInput.substring(strKey.length());
			}
		}
		return strInput;
	}

	/**
	 * 判断是否为空,null,"null",""都算是空
	 */
	public static boolean isNull(Object object)
	{
		boolean bR = false;
		if (object == null)
		{
			bR = true;
		}else if ("".equals(object))
		{
			bR = true;
		}else if ("null".equals(object))
		{
			bR = true;
		}
		return bR;
	}
	
	/**
	 * 得到Server端的毫秒数
	 */
	public static long getServerMS()
	{
		return Calendar.getInstance().getTimeInMillis();
	}

	/**
	 * 将整数形式的秒转换为简单日期时间的字符串
	 */
	public static String IntTime2SimpleStr(long Time)
	{
		if (Time == 0)
			return "";
		Date tempDate = new Date(Time * 1000);
		SimpleDateFormat dateformat = new SimpleDateFormat("MM-dd HH:mm");
		return dateformat.format(tempDate);
	}
	
	
	/**
	 * 将整数形式的秒转换为指定格式的日期字符串
	 */
	public static String LongTime2FormatStr(long TimeMillis, String format)
	{
		if (format == null)
			return "";
		Date tempDate = new Date(TimeMillis);
		SimpleDateFormat dateformat = new SimpleDateFormat(format);
		try
		{
			dateformat = new SimpleDateFormat(format);
		}catch (Exception e)
		{
			logger.error("异常:", e);
		}
		return dateformat.format(tempDate);
	}

	/**
	 * 得到指定长度的随机码
	 * 
	 * @param nLength
	 * @return
	 */
	public static String getRandCode(int nLength)
	{
		String strRandCode = "";
		for (int i = 0; i < nLength; i++)
		{
			int randomChar = 76;
			while (true) // 保证是数字和字母
			{
				randomChar = 48 + (new Double(Math.random() * 1000)).intValue() % 74;
				if ((randomChar >= '0' && randomChar <= '9') || (randomChar >= 'a' && randomChar <= 'z'))
				{
					break;
				}
			}
			strRandCode += new Character((char) randomChar);
		}
		return strRandCode;
	}
	
	/**
	 * @param listFile
	 * @param strDestFile
	 * @return
	 */
	public static String FileCombine(List<String> listFile, String strDestFile)
	{
		InputStreamReader read = null;
		try {
			OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(strDestFile,true),"UTF-8");
			BufferedWriter bufferWritter = new BufferedWriter(writerStream);
			for (int i = 0; i < listFile.size(); i++) {
				read = new InputStreamReader(new FileInputStream(listFile.get(i)), "UTF-8");
				BufferedReader in = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = in.readLine()) != null) {
					if(!lineTxt.equals(""))
					{
						bufferWritter.write(lineTxt+"\n");
					}
				}
				in.close();
				read.close();
			}
			bufferWritter.close();
		} catch (Exception e) {
			logger.error(e);
		}
		return strDestFile;
	}
	
	
	/**
	 * 判断目录是否存在，不存在则创建
	 * @return
	 */
	public static String getSonDir(String strParentPath)
	{
		String strDate = PubFunc.LongTime2FormatStr(PubFunc.getServerMS(), "yyyy-MM-dd");
		try 
		{
			File errorDir = new File(strParentPath + strDate);
			if ((!errorDir.exists()) || (!errorDir.isDirectory())) {
				errorDir.mkdir();
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return strParentPath + strDate;
	}
}
