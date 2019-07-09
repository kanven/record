package com.kanven.record.ext.plugins.extract.db.parser;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.kanven.record.exception.RecordException;

public final class Schema {

	/**
	 * 数据库前缀
	 */
	private final String dbPrefix;

	/**
	 * 数据库后缀
	 */
	private final String dbSuffix;

	/**
	 * 数据库全称
	 */
	private final String db;

	/**
	 * 数据库表
	 */
	private final Map<String, Table> tables;

	private Schema(String prifix, String suffix, String db, Map<String, Table> tables) {
		if (StringUtils.isBlank(db) && StringUtils.isBlank(prifix) && StringUtils.isBlank(suffix)) {
			throw new RecordException("数据库名不能为空！");
		}
		this.dbPrefix = prifix;
		this.dbSuffix = suffix;
		this.db = db;
		if (tables == null || tables.isEmpty()) {
			throw new RecordException("数据库表不能为空！");
		}
		this.tables = tables;
	}

	public final String prifix() {
		return this.dbPrefix;
	}

	public final String suffix() {
		return this.dbSuffix;
	}

	public final String db() {
		return this.db;
	}

	public final boolean satifyDb(String db) {
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

	public final boolean hasTable(String table) {
		return tables.containsKey(table);
	}

	public final Table Table(String name) {
		return tables.get(name);
	}

	@Override
	public String toString() {
		return "Schema [dbPrefix=" + dbPrefix + ", dbSuffix=" + dbSuffix + ", db=" + db + ", tables=" + tables + "]";
	}

	public static class SchemaBuilder {

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

		private Map<String, Table> tables = new HashMap<>();

		private SchemaBuilder() {

		}

		public static SchemaBuilder newInstance() {
			return new SchemaBuilder();
		}

		public SchemaBuilder prefix(String prefix) {
			this.dbPrefix = prefix;
			return this;
		}

		public SchemaBuilder suffix(String suffix) {
			this.dbSuffix = suffix;
			return this;
		}

		public SchemaBuilder db(String db) {
			this.db = db;
			return this;
		}

		public SchemaBuilder table(Table table) {
			this.tables.put(table.name(), table);
			return this;
		}

		public Schema build() {
			return new Schema(dbPrefix, dbSuffix, db, tables);
		}

	}

}
