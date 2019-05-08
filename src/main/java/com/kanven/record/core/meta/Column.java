package com.kanven.record.core.meta;

import java.io.Serializable;

import com.kanven.record.exception.RecordException;
import org.apache.commons.lang3.StringUtils;

/**
 * 表列
 * 
 * @author kanven
 *
 */
public final class Column implements Serializable {

	private static final long serialVersionUID = -2906138107430265053L;

	/**
	 * 列索引
	 */
	private final Integer index;

	/**
	 * 列名
	 */
	private final String name;

	/**
	 * 类型
	 */
	private final String type;

	/**
	 * 值
	 */
	private final Object value;

	/**
	 * 是否是主键
	 */
	private final boolean key;

	public Column(Integer index, String name, String type, Object value, boolean key) {
		if (index == null || index < 0) {
			throw new RecordException("the column index can't be null or negative");
		}
		if (StringUtils.isBlank(name)) {
			throw new RecordException("the column name can't be null");
		}
		if (StringUtils.isBlank(type)) {
			throw new RecordException("the column type can't be null");
		}
		this.index = index;
		this.name = name;
		this.type = type;
		this.value = value;
		this.key = key;
	}

	public Integer index() {
		return index;
	}

	public String name() {
		return name;
	}

	public String type() {
		return type;
	}

	public Object value() {
		return value;
	}

	public boolean isKey() {
		return key;
	}

	@Override
	public int hashCode() {
		int hash = 31 * index;
		hash = 31 * hash + name.hashCode();
		hash = 31 * hash + type.hashCode();
		if (value != null) {
			hash = 31 * hash + value.hashCode();
		}
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof Column)) {
			return false;
		}
		Column column = (Column) o;
		boolean result = index.equals(column.index) && name.equals(column.name) && type.equals(column.type);
		if (value == column.value) {
			result = result && true;
		} else if (value != null) {
			result = result && value.equals(column.value);
		} else {
			result = result && column.value.equals(value);
		}
		return result;
	}

	@Override
	public String toString() {
		return "Column [index=" + index + ", name=" + name + ", type=" + type + ", value=" + value + ", key=" + key
				+ "]";
	}

	public static class ColumnBuilder {

		private Integer index;

		private String name;

		private String type;

		private Object value;

		private boolean key;

		public static ColumnBuilder newBuilder() {
			return new ColumnBuilder();
		}

		public ColumnBuilder index(Integer index) {
			this.index = index;
			return this;
		}

		public ColumnBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ColumnBuilder type(String type) {
			this.type = type;
			return this;
		}

		public ColumnBuilder value(Object value) {
			this.value = value;
			return this;
		}

		public ColumnBuilder key(boolean key) {
			this.key = key;
			return this;
		}

		public Column build() {
			return new Column(index, name, type, value, key);
		}

	}

}
