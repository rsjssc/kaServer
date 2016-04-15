// ********************************************************************************//
// File Name : DBBridge.java //
// Author : clm //
// Created time : 2004-10-8 //
// Description : Get real time statistics from servlet and display through
// applet//
// //
// //
// Copyright (c) 1999-2004 ATM R&D Center, BUPT. All Rights Reserved. //
// ********************************************************************************//
package com.database;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.util.PubFunc;

public class DBBridge
{
	private static Logger  logger  = Logger.getLogger(DBBridge.class);
	private Connection			conn			= null;
	private ResultSet			rs				= null;
	private Statement			stmt			= null;
	private PreparedStatement	pstmt			= null;
	private String				db_url			= "";

	public DBBridge()
	{
		this.db_url = DBPara.DB_URL;
	}

	public DBBridge(String dburl)
	{
		this.db_url = dburl;
	}

	public String getDb_url()
	{
		return db_url;
	}

	public void setDb_url(String db_url)
	{
		this.db_url = db_url;
	}
	
	
	public void openBridge() throws Exception
	{
		//通过连接池进行连接
		int nLinkCount = DBPara.linkCountUp();
		clearResult();
		if (nLinkCount >= DBPara.DB_MAX_CONN)
		{
			throw new SQLException("地址连接异常，请重启系统后再请求!");
		}
		try
		{
			Class.forName(DBPara.DB_DRIVER_NAME).newInstance();
			conn = DriverManager.getConnection("jdbc:postgresql://" + this.db_url + ":" + DBPara.DB_PORT + "/" + DBPara.DB_BASENAME +"?user=" + DBPara.DB_USERNAME + "&password="+DBPara.DB_PASS+"&loginTimeout=5");
		
		}catch (Exception e)
		{
			logger.error("异常:",e);
		}
		logger.info("<--Request a Remote DB Connect:" + this.db_url + "(jdbc)(" + nLinkCount + ")");
		return;
	}

	public void closeBridge() throws SQLException
	{
		clearResult();
		if (!PubFunc.isNull(this.db_url))
		{
			if (conn != null)
			{
				conn.close();
				conn = null;
			}
			int nLinkCount = DBPara.linkCountDown();
			logger.info("-->Return a Remote DB Connect:" + this.db_url + "(jdbc)(" + nLinkCount + ")");
		}
	}

	private void clearResult() throws SQLException
	{
		if (rs != null)
			rs.close();
		if (stmt != null)
			stmt.close();
		if (pstmt != null)
			pstmt.close();
		rs = null;
		stmt = null;
		pstmt = null;
	}

	public ResultSet execSELECT(String sqls) throws SQLException
	{
		logger.info("" + sqls);
		if (conn == null)
			throw new SQLException("连接还没有被建立!");
		if (sqls == null)
			throw new SQLException("SQL-statement是null!");
		clearResult();
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqls);
		}catch (SQLException e)
		{
			logger.error("异常:",e);
			if (e.toString().toLowerCase().indexOf("unknown response type") != -1)
			{
				// connectionPool.reTryConnection(conn);
				throw (new SQLException("数据库连接错误,请稍候重试!"));
			}else
			{
				throw e;
			}
		}
		return rs;
	}

	/**
	 * 执行批处理,lxw,2005-10-27
	 * 
	 * @param sqls
	 *            String
	 * @throws SQLException
	 * @return int[]
	 */
	public int[] executeBatUpdate(String[] sqls) throws SQLException
	{
		// kg.beap.LogFile.AddToLog(""+sqls);
		if (conn == null)
			throw new SQLException("连接还没有被建立!");
		if (sqls == null)
			throw new SQLException("SQL-statement是null!");
		clearResult();
		conn.setAutoCommit(true);
		stmt = conn.createStatement();
		for (int i = 0; i < sqls.length; i++)
		{
			logger.info("" + sqls[i]);
			stmt.addBatch(sqls[i]);
		}
		int[] numRow = stmt.executeBatch();
		return numRow;
	}

	//
	public int[] executeBatUpdate(ArrayList sqlArr) throws SQLException
	{
		String[] sqls = new String[sqlArr.size()];
		for (int i = 0; i < sqls.length; i++)
		{
			sqls[i] = sqlArr.get(i).toString();
		}
		// kg.beap.LogFile.AddToLog(""+sqls);
		if (conn == null)
			throw new SQLException("连接还没有被建立!");
		if (sqls == null)
			throw new SQLException("SQL-statement是null!");
		clearResult();
		conn.setAutoCommit(true);
		stmt = conn.createStatement();
		for (int i = 0; i < sqls.length; i++)
		{
			logger.info("" + sqls[i]);
			stmt.addBatch(sqls[i]);
		}
		int[] numRow = stmt.executeBatch();
		return numRow;
	}

	/**
	 * 执行更新,lxw,2005-4-15
	 * 
	 * @param sqls
	 *            String
	 * @throws SQLException
	 * @return int 经测试发现经此方法可以正确执行中文的插入和修改,而本类中的其他方法则有可能不正常。
	 */
	public int executeUpdate(String sqls) throws SQLException
	{
		logger.info("" + sqls);
		if (conn == null)
			throw new SQLException("连接还没有被建立!");
		if (sqls == null)
			throw new SQLException("SQL-statement是null!");
		clearResult();
		conn.setAutoCommit(true);
		stmt = conn.createStatement();
		int numRow = stmt.executeUpdate(sqls);
		return numRow;
	}

	/**
	 * 执行更新,lxw,2009-7-22
	 * 
	 * @param sqls
	 *            String
	 * @throws SQLException
	 * @return int 不进行AutoCommit
	 */
	public int executeUpdate(String sqls, boolean autocommit) throws SQLException
	{
		logger.info("" + sqls);
		if (conn == null)
			throw new SQLException("连接还没有被建立!");
		if (sqls == null)
			throw new SQLException("SQL-statement是null!");
		clearResult();
		conn.setAutoCommit(autocommit);
		stmt = conn.createStatement();
		int numRow = stmt.executeUpdate(sqls);
		return numRow;
	}

	public int execSQL(String sqls, String args[]) throws SQLException
	{
		logger.info("" + sqls);
		if (conn == null)
			throw new SQLException("连接还没有被建立!");
		if (sqls == null)
			throw new SQLException("SQL-statement是null!");
		clearResult();
		conn.setAutoCommit(true);
		pstmt = conn.prepareStatement(sqls);
		if (args != null)
			for (int i = 0; i < args.length; i++)
			{
				int len = 0;
				String addStr = "";
				for (int j = 0; j < args[i].length(); j++)
				{
					if (args[i].charAt(j) > 0x7f)
						len++;
				}
				for (int j = 0; j < len; j += 2)
					addStr += " ";
				String temp = null;
				try
				{
					temp = new String((args[i] + addStr).getBytes("ISO8859_1"), "gb2312");
				}catch (Exception e)
				{
					logger.error("异常:",e);
					temp = args[i] + addStr;
				}
				pstmt.setString(i + 1, temp);
			}
		int numRow = pstmt.executeUpdate();
		return numRow;
	}

	public int execSQLs(String sqls, String args[][]) throws SQLException
	{
		logger.info("" + sqls);
		if (conn == null)
			throw new SQLException("连接还没有被建立!");
		if (sqls == null)
			throw new SQLException("SQL-statement是null!");
		clearResult();
		conn.setAutoCommit(false);
		pstmt = conn.prepareStatement(sqls);
		try
		{
			for (int i = 0; i < args.length; i++)
			{
				for (int ii = 0; ii < args[i].length; ii++)
				{
					int len = 0;
					String addStr = "";
					for (int j = 0; j < args[i][ii].length(); j++)
					{
						if (args[i][ii].charAt(j) > 0x7f)
							len++;
					}
					for (int j = 0; j < len; j += 2)
						addStr += " ";
				}
				pstmt.executeUpdate();
			}
			conn.commit();
		}catch (SQLException ex)
		{
			conn.rollback();
			throw ex;
		}
		return 0;
	}

	public boolean nextRow() throws SQLException
	{
		if (rs == null)
			throw new SQLException("ResultSet是null!");
		return rs.next();
	}

	public String getString(String fieldName) throws SQLException
	{
		if (rs == null)
			throw new SQLException("ResultSet是null!");
		return rs.getString(fieldName);
	}

	public int getInt(String fieldName) throws SQLException
	{
		if (rs == null)
			throw new SQLException("ResultSet是null!");
		return rs.getInt(fieldName);
	}

	public long getLong(String fieldName) throws SQLException
	{
		if (rs == null)
			throw new SQLException("ResultSet是null!");
		return rs.getLong(fieldName);
	}

	public float getFloat(String fieldName) throws SQLException
	{
		if (rs == null)
			throw new SQLException("ResultSet是null!");
		return rs.getFloat(fieldName);
	}

	public BigDecimal getBigDecimal(String fieldName) throws SQLException
	{
		if (rs == null)
			throw new SQLException("ResultSet是null!");
		return rs.getBigDecimal(fieldName);
	}

	public double getDouble(String fieldName) throws SQLException
	{
		if (rs == null)
			throw new SQLException("ResultSet是null!");
		return rs.getDouble(fieldName);
	}

	public Timestamp getTimestamp(String fieldName) throws SQLException
	{
		if (rs == null)
			throw new SQLException("ResultSet是null!");
		return rs.getTimestamp(fieldName);
	}

	public boolean getBoolean(String fieldName) throws SQLException
	{
		if (rs == null)
			throw new SQLException("ResultSet是null!");
		return rs.getBoolean(fieldName);
	}

	public byte getByte(String fieldName) throws SQLException
	{
		if (rs == null)
			throw new SQLException("ResultSet是null(getByte)!");
		return rs.getByte(fieldName);
	}

	public byte[] getBytes(String fieldName) throws SQLException
	{
		if (rs == null)
			throw new SQLException("ResultSet是null!");
		return rs.getBytes(fieldName);
	}

	public int getRow() throws SQLException
	{
		if (rs == null)
			throw new SQLException("ResultSet是null(getRow)!");
		return rs.getRow();
	}

	public void beforeFirst() throws SQLException
	{
		if (rs == null)
			throw new SQLException("ResultSet是null(beforeFirst)!");
		rs.beforeFirst();
	}

	public void afterLast() throws SQLException
	{
		if (rs == null)
			throw new SQLException("ResultSet是null(afterLast)!");
		rs.afterLast();
	}
}
