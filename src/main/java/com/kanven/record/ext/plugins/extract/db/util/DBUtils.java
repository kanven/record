package com.kanven.record.ext.plugins.extract.db.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.kanven.record.exception.RecordException;
import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author kanven
 *
 */
public class DBUtils {

	/**
	 * 获取数据库名列表
	 * 
	 * @param conn
	 *            数据库连接
	 * @return
	 * @throws SQLException
	 */
	public static String[] getSchemes(Connection conn) throws SQLException {
		DatabaseMetaData meta = conn.getMetaData();
		String driver = meta.getDriverName();
		if (driver.toUpperCase().indexOf("MYSQL") > 0) {
			String cata = conn.getCatalog();
			if (StringUtils.isNotBlank(cata)) {
				return new String[] { cata };
			} else {
				ResultSet rs = meta.getCatalogs();
				String[] catas = new String[rs.getFetchSize()];
				while (rs.next()) {
					catas[rs.getRow() - 1] = rs.getString("TABLE_CAT");
				}
				return catas;
			}
		}
		throw new RecordException("the driver(" + driver + ") unsupport");
	}

	/**
	 * 获取数据库表名列表
	 * 
	 * @param conn
	 *            数据库连接
	 * @param schema
	 *            数据库名
	 * @return
	 * @throws SQLException
	 */
	public static String[] getTables(Connection conn, String schema) throws SQLException {
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getTables(schema, null, null, new String[] { "MYSQL" });
		String[] tables = new String[rs.getFetchSize()];
		while (rs.next()) {
			tables[rs.getRow() - 1] = rs.getString("TABLE_NAME");
		}
		return tables;
	}

	/**
	 * 获取表列名列表
	 * 
	 * @param conn
	 *            数据库连接
	 * @param schema
	 *            数据库名
	 * @param table
	 *            表名
	 * @return
	 * @throws SQLException
	 */
	public static String[] getColumns(Connection conn, String schema, String table) throws SQLException {
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getColumns(schema, null, table, null);
		String[] columns = new String[rs.getFetchSize()];
		while (rs.next()) {
			columns[rs.getRow() - 1] = rs.getString("COLUMN_NAME");
		}
		return columns;
	}

}
