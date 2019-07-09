package com.kanven.record.ext.plugins.extract.db.parser;

import java.util.HashSet;
import java.util.Set;

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
	private final Set<Table> tables;

	private Schema(String prifix, String suffix, String db, Set<Table> tables) {
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

	public final Set<Table> tables() {
		return this.tables;
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

		private Set<Table> tables = new HashSet<>();

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
			this.tables.add(table);
			return this;
		}

		public Schema build() {
			return new Schema(dbPrefix, dbSuffix, db, tables);
		}

	}

}
