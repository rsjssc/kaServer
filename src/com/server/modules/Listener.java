package com.server.modules;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;


public class Listener implements ServletContextListener{

	private static Logger log = Logger.getLogger(Listener.class);
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		log.info("KA WebServer is close...");
		shutdown kaShutDownWork = new shutdown();
		kaShutDownWork.kaMonitorShutdownWork();
		log.info("KA WebServer stop!");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
//		// TODO Auto-generated method stub
		log.info("KA WebServer is start!");
//		
//		getProperties getConfigs = new getProperties(); 
//		String url_postgres = getConfigs.getUrl_postgres();
//		String user_postgres = getConfigs.getUser_postgres();
//		String password_postgres = getConfigs.getPassword_postgres();
//		
//		String ip_redis = getConfigs.getIp_redis();
//		int port_redis = getConfigs.getPort_redis();
//		
//		
//		Initialize Init = new Initialize(url_postgres, user_postgres, password_postgres, ip_redis, port_redis);
//		Init.readFromPostgresql();
//		_scheduleMonitor = Executors.newSingleThreadScheduledExecutor();
//		int monitorCircle = 180;
//		_scheduleMonitor.scheduleWithFixedDelay(new Monitor(url_postgres, user_postgres, password_postgres, ip_redis, port_redis),
//				0, monitorCircle, TimeUnit.SECONDS);
		
	}

}
