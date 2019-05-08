package com.kanven.record.core.meta;

import java.io.Serializable;
import java.util.HashSet;

import java.util.Set;

import com.kanven.record.exception.RecordException;
import org.apache.commons.lang3.StringUtils;

/**
 * 表行
 * 
 * @author kanven
 *
 */
public final class Row implements Serializable {

	private static final long serialVersionUID = -1743694820082064824L;

	private String schema;

	private final String table;

	private final Set<Column> primaryKey;

	private final Set<Column> olds;

	private final Set<Column> columns;

	private Long executeTime;

	public Row(String schema, String table, Set<Column> primaryKey, Set<Column> olds, Set<Column> columns,
			Long executeTime) {
		if (StringUtils.isBlank(schema)) {
			throw new RecordException("the row's schema should not be null");
		}
		this.schema = schema;
		if (StringUtils.isBlank(table)) {
			throw new RecordException("the row's table should not be null");
		}
		this.table = table;
		this.primaryKey = primaryKey;
		if (columns == null || columns.isEmpty()) {
			throw new RecordException("the row's columns should not be null");
		}
		this.olds = olds;
		this.columns = columns;
		if (executeTime == null) {
			executeTime = System.currentTimeMillis();
		}
		this.executeTime = executeTime;
	}

	public String schema() {
		return schema;
	}

	public void schema(String schema) {
		this.schema = schema;
	}

	public String table() {
		return table;
	}

	public Set<Column> primaryKey() {
		return primaryKey;
	}

	public Set<Column> columns() {
		return columns;
	}

	public Set<Column> olds() {
		return olds;
	}

	public Long executeTime() {
		return executeTime;
	}

	@Override
	public String toString() {
		return "Row [schema=" + schema + ", table=" + table + ", primaryKey=" + primaryKey + ", columns=" + columns
				+ ", executeTime=" + executeTime + "]";
	}

	public static class RowBuilder {

		private String schema;

		private String table;

		private Set<Column> primaryKey = new HashSet<>(0);

		private Set<Column> olds = new HashSet<>(0);

		private Set<Column> columns = new HashSet<>(0);

		private Long executeTime;

		public static RowBuilder newBuilder() {
			return new RowBuilder();
		}

		public RowBuilder schema(String schema) {
			this.schema = schema;
			return this;
		}

		public RowBuilder table(String table) {
			this.table = table;
			return this;
		}

		public RowBuilder primaryKey(Column column) {
			if (column != null) {
				this.primaryKey.add(column);
			}
			return this;
		}

		public RowBuilder old(Column old) {
			if (old != null) {
				this.olds.add(old);
			}
			return this;
		}

		public RowBuilder olds(Set<Column> olds) {
			if (olds != null) {
				this.olds.addAll(olds);
			}
			return this;
		}

		public RowBuilder column(Column column) {
			if (column != null) {
				this.columns.add(column);
			}
			return this;
		}

		public RowBuilder columns(Set<Column> columns) {
			if (columns != null) {
				this.columns.addAll(columns);
			}
			return this;
		}

		public RowBuilder executeTime(Long executeTime) {
			this.executeTime = executeTime;
			return this;
		}

		public Row build() {
			if (StringUtils.isBlank(schema)) {
				throw new RecordException("schema can't be null");
			}
			if (StringUtils.isBlank(table)) {
				throw new RecordException("table name can't be null");
			}
			return new Row(schema, table, primaryKey, olds, columns, executeTime);
		}

	}

}
