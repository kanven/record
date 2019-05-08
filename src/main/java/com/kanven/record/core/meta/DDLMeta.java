package com.kanven.record.core.meta;

import java.io.Serializable;

/**
 * DDL元数据
 * 
 * @author kanven
 *
 */
public final class DDLMeta implements Serializable {

	private static final long serialVersionUID = 8283181116077166327L;

	private final String schema;

	private final String table;

	private final String sql;

	public DDLMeta(String schema, String table, String sql) {
		this.schema = schema;
		this.table = table;
		this.sql = sql;
	}

	public String schema() {
		return this.schema;
	}

	public String table() {
		return this.table;
	}

	public String sql() {
		return this.sql;
	}

	@Override
	public String toString() {
		return "DDLMeta [schema=" + schema + ", table=" + table + ", sql=" + sql + "]";
	}

	public static class DDLMetaBuilder {

		private String schema;

		private String table;

		private String sql;

		public static DDLMetaBuilder newBuilder() {
			return new DDLMetaBuilder();
		}

		public DDLMetaBuilder schema(String schema) {
			this.schema = schema;
			return this;
		}

		public DDLMetaBuilder table(String table) {
			this.table = table;
			return this;
		}

		public DDLMetaBuilder sql(String sql) {
			this.sql = sql;
			return this;
		}

		public DDLMeta build() {
			return new DDLMeta(schema, table, sql);
		}

	}

}
