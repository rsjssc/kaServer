package com.system;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.database.DBPara;
import com.util.PubPara;

public class ReadSysConfig
{
	private static Logger  logger  = Logger.getLogger(ReadSysConfig.class);
	
	/**
	 * 读取配置文件
	 * 
	 * @param pathfile
	 */
	public void readConfigFile(String pathfile)
	{
		pathfile = pathfile + "beaprun.cfg";
		BufferedReader d = null;
		try
		{
			d = new BufferedReader(new InputStreamReader(new FileInputStream(pathfile)));
			while (true)
			{
				String line = d.readLine();
				if (line == null)
					break;
				try
				{
					String[] para = line.split("=");
					if (para.length > 1)
					{
						String cfgName = para[0].trim();
						if (cfgName.equalsIgnoreCase("driver"))
						{
							DBPara.DB_DRIVER_NAME = para[1].trim();
						}else if (cfgName.equalsIgnoreCase("DBurl"))
						{
							DBPara.setDB_URL(para[1].trim());
						}else if (cfgName.equalsIgnoreCase("username"))
						{
							DBPara.setDB_USERNAME(para[1].trim());
						}else if (cfgName.equalsIgnoreCase("password"))
						{
							DBPara.setDB_PASS(para[1].trim());
						}else if (cfgName.equalsIgnoreCase("databaseName"))
						{
							DBPara.setDB_BASENAME(para[1].trim());
						}else if (cfgName.equalsIgnoreCase("portNumber"))
						{
							int port = Integer.parseInt(para[1].trim());
							if (port > 0)
								DBPara.setDB_PORT(port);
						}else if (cfgName.equalsIgnoreCase("maxconnect"))
						{
							int maxconnect = Integer.parseInt(para[1].trim());
							if (maxconnect > 0)
								DBPara.setDB_MAX_CONN(maxconnect);
						}else if (cfgName.equalsIgnoreCase("KaUrl"))
						{
							PubPara.strKaUrl = para[1].trim();
						}else if (cfgName.equalsIgnoreCase("KaTime"))
						{
							int katime = Integer.parseInt(para[1].trim());
							if (katime > 0)
								PubPara.nKaTime = katime;	
						}else if (cfgName.equalsIgnoreCase("FtpUrl"))
						{
							PubPara.strSftpUrl = para[1].trim();
						}else if (cfgName.equalsIgnoreCase("FtpPort"))
						{
							PubPara.strSftpPort = para[1].trim();
						}else if (cfgName.equalsIgnoreCase("FtpAccount"))
						{
							PubPara.strSftpAccount = para[1].trim();
						}else if (cfgName.equalsIgnoreCase("FtpPassword"))
						{
							PubPara.strSftpPassword = para[1].trim();
						}else if (cfgName.equalsIgnoreCase("FtpTime"))
						{
							int sftptime = Integer.parseInt(para[1].trim());
							if (sftptime > 0)
								PubPara.nSftpTime = sftptime;	
						}else if (cfgName.equalsIgnoreCase("EupdateUrl"))
						{
							PubPara.strEupdateUrl = para[1].trim();
						}else if (cfgName.equalsIgnoreCase("EupdatePort"))
						{
							PubPara.strEupdatePort = para[1].trim();
						}else if (cfgName.equalsIgnoreCase("EupdateProtocol"))
						{
							PubPara.strEupdateProtocol = para[1].trim();
						}else if (cfgName.equalsIgnoreCase("EupdateAccount"))
						{
							PubPara.strEupdateAccount = para[1].trim();
						}else if (cfgName.equalsIgnoreCase("EupdatePassword"))
						{
							PubPara.strEupdatePassword = para[1].trim();
						}else if (cfgName.equalsIgnoreCase("RedisIP"))
						{
							PubPara.strRedisIP = para[1].trim();
						}else if (cfgName.equalsIgnoreCase("RedisPort"))
						{
							int redisport = Integer.parseInt(para[1].trim());
							if (redisport > 0)
								PubPara.nRedisPort = redisport;	
						}
					}
				}catch (Exception e)
				{
					logger.error("异常:",e);
				}
			}
		}catch (Exception e)
		{
			logger.error("异常:",e);
		}finally
		{
			try
			{
				if (d != null)
				{
					d.close();
				}
			}catch (Exception ee)
			{
			}
		}
	}
	
}
