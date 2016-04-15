package com.system;

import java.io.File;

import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import com.ftpdata.DealBcpFilesThread;
import com.ftpdata.ScanSftpDataThread;
import com.ftpdata.SftpMonitor;
import com.server.modules.Initialize;
import com.server.modules.Monitor;
import com.util.PubFunc;
import com.util.PubString;
/**
 * 概述：平台起动加载项<br>
 * 
 * @author zhangy<br>
 * @version StartupServer.java 2011-11-10 下午03:46:00<br>
 * <br>
 */
public class StartupServer extends HttpServlet
{
	private static final long	serialVersionUID	= 1L;
	private static Logger  logger  = Logger.getLogger(StartupServer.class);

	public StartupServer()
	{
	}

	public void init()
	{
		try
		{
			logger.info("StartupServer Begin ...");
			// 取得ROOT的绝对路径
			PubFunc.setRootPath(this.getServletContext().getRealPath("/"));
			// 取得data的绝对路径
			String configpath = this.getServletContext().getRealPath("WEB-INF") + "/data/";
			PubFunc.setDATA_CONFIG_PATH(configpath);
			logger.info("11111111111:::::" + configpath);
			//准备好文件目录
			createWorkFolder();
			
			// 配置文件的读取
			(new ReadSysConfig()).readConfigFile(configpath);
			
			//读取数据库到redis
			Initialize Init = new Initialize();
			Init.readFromPostgresql();
			
			//ka在线状态周期性模块
			(new Monitor()).start();
			
			//数据在线状态周期性模块
			(new DealBcpFilesThread()).start();
			(new ScanSftpDataThread()).start();
			(new SftpMonitor()).start();
			
			//清除备份文件
			(new ClearDataThread()).start();
			
		}catch (Exception e)
		{
			logger.error("异常:", e);
		}
	}
	
	
	/**
	 * 创建目录
	 */
	private void createWorkFolder()
	{
		logger.info("createWorkFolder");
		String folders[] = { PubString.strUnzipPath,//
				PubString.strTempPath,//
				PubString.strHdfsPath,
				PubString.strErrorDataPath,//
				PubString.strBackDataPath
		};
		for (int i = 0; i < folders.length; i++)
		{
			try
			{
				File folder = new File(folders[i]);
				if (!folder.exists())
				{
					folder.mkdirs();
					logger.info("mkdir: " + folders[i]);
					PubFunc.execSysCmd("chmod 777 " + folders[i]);
				}
			}catch (Exception e)
			{
				logger.error("error:", e);
			}
		}
	}

}
