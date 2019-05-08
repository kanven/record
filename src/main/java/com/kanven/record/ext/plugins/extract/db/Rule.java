package com.kanven.record.ext.plugins.extract.db;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.kanven.record.exception.RecordException;
import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author kanven
 *
 */
public class Rule {

	/**
	 * 数据库前缀
	 */
	private String dbPrefix;

	/**
	 * 数据库后缀
	 */
	private String dbSuffix;

	/**
	 * 数据库全称
	 */
	private String db;

	/**
	 * 表映射关系
	 */
	private Map<String, Map<String, ColumnRule>> mapping = new HashMap<>();

	public boolean hasTable(String table) {
		return mapping.containsKey(table);
	}

	public Set<String> getColumns(String table) {
		Map<String, ColumnRule> mapping = this.mapping.get(table);
		if (mapping == null) {
			throw new RecordException("the table(" + table + ") mapping not exist");
		}
		return mapping.keySet();
	}

	public boolean isIgnore(String table, String column) {
		Map<String, ColumnRule> mapping = this.mapping.get(table);
		if (mapping == null) {
			throw new RecordException("the table(" + table + ") mapping not exist");
		}
		if (mapping.containsKey(column)) {
			ColumnRule rule = mapping.get(column);
			if (rule != null) {
				return rule.ignore;
			}
		}
		return false;
	}

	public boolean satifyDb(String db) {
		if (StringUtils.isBlank(dbPrefix) && StringUtils.isBlank(dbSuffix)) {
			return this.db.equals(db);
		}
		if (StringUtils.isNoneBlank(dbPrefix) && StringUtils.isNoneBlank(dbSuffix)) {
			return db.startsWith(dbPrefix) && db.endsWith(dbSuffix);
		}
		if (StringUtils.isNotBlank(dbPrefix)) {
			return db.startsWith(dbPrefix);
		} else {
			return db.endsWith(dbSuffix);
		}
	}

	public void addMapping(String table, Map<String, ColumnRule> columns) {
		mapping.put(table, columns);
	}

	public void setDbPrefix(String dbPrefix) {
		this.dbPrefix = dbPrefix;
	}

	public void setDbSuffix(String dbSuffix) {
		this.dbSuffix = dbSuffix;
	}

	public void setDb(String db) {
		this.db = db;
	}

	@Override
	public String toString() {
		return "Rule [dbPrefix=" + dbPrefix + ", dbSuffix=" + dbSuffix + ", db=" + db + ", mapping=" + mapping + "]";
	}

	public static class ColumnRule implements Serializable {

		private static final long serialVersionUID = -5675267921197006592L;

		private boolean ignore = false;

		public boolean isIgnore() {
			return ignore;
		}

		public void setIgnore(boolean ignore) {
			this.ignore = ignore;
		}

		@Override
		public String toString() {
			return "ColumnRule [ignore=" + ignore + "]";
		}

	}

}
