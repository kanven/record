package com.kanven.record.ext.plugins.extract.db.parser;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.kanven.record.exception.RecordException;

public final class Table implements Serializable {

	private static final long serialVersionUID = -8556637008324874203L;

	/**
	 * 表名
	 */
	private final String name;

	/**
	 * 字段
	 */
	private final Map<String, Field> fields;

	/**
	 * 是否所有字段
	 */
	private final boolean all;

	private Table(String name, Map<String, Field> fields, boolean all) {
		if (StringUtils.isBlank(name)) {
			throw new RecordException("表名不能为空！");
		}
		this.name = name;
		this.all = all;
		Map<String, Field> fs = null;
		if (all) {
			fs = new HashMap<>(0);
		} else {
			if (fields == null || fields.isEmpty()) {
				throw new RecordException(name + "表字段不能为空！");
			}
			fs = fields;
		}
		this.fields = Collections.unmodifiableMap(fs);
	}

	public final String name() {
		return this.name;
	}

	public final boolean all() {
		return this.all;
	}

	public final Field field(String name) {
		return this.fields.get(name);
	}

	public boolean hasField() {
		return !this.fields.isEmpty();
	}

	public boolean contains(String name) {
		return fields.containsKey(name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (all ? 1231 : 1237);
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Table other = (Table) obj;
		if (all != other.all)
			return false;
		if (fields == null) {
			if (other.fields != null)
				return false;
		} else if (!fields.equals(other.fields))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Table [name=" + name + ", fields=" + fields + ", all=" + all + "]";
	}

	public static class TableBuilder {

		private String name;

		private Map<String, Field> fields = new HashMap<>();

		private boolean all;

		private TableBuilder() {

		}

		public static TableBuilder newInstance() {
			return new TableBuilder();
		}

		public TableBuilder name(String name) {
			this.name = name;
			return this;
		}

		public TableBuilder all(boolean all) {
			this.all = all;
			return this;
		}

		public TableBuilder field(Field field) {
			this.fields.put(field.name, field);
			return this;
		}

		public Table build() {
			return new Table(name, fields, all);
		}

	}

	public final static class Field implements Serializable {

		private static final long serialVersionUID = -3928382273902883445L;

		private final String name;

		private final boolean ignored;

		private Field(String name, boolean ignored) {
			if (StringUtils.isBlank(name)) {
				throw new RecordException("数据库字段不能为空！");
			}
			this.name = name;
			this.ignored = ignored;
		}

		public final String name() {
			return this.name;
		}

		public final boolean ignored() {
			return this.ignored;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (ignored ? 1231 : 1237);
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Field other = (Field) obj;
			if (ignored != other.ignored)
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Field [name=" + name + ", ignored=" + ignored + "]";
		}

		public static class FieldBuilder {

			private String name;

			private boolean ignored;

			private FieldBuilder() {

			}

			public static FieldBuilder newInstance() {
				return new FieldBuilder();
			}

			public FieldBuilder name(String name) {
				this.name = name;
				return this;
			}

			public FieldBuilder ignored(boolean ignored) {
				this.ignored = ignored;
				return this;
			}

			public Field build() {
				return new Field(name, ignored);
			}

		}

	}

}
