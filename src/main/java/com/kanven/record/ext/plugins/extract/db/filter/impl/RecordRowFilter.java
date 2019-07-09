package com.kanven.record.ext.plugins.extract.db.filter.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kanven.record.core.meta.Column;
import com.kanven.record.core.meta.Row;
import com.kanven.record.exception.RecordException;
import com.kanven.record.ext.plugins.extract.db.filter.Filter;
import com.kanven.record.ext.plugins.extract.db.parser.Schema;
import com.kanven.record.ext.plugins.extract.db.parser.SchemaParseHandler;
import com.kanven.record.ext.plugins.extract.db.parser.Table;
import com.kanven.record.ext.plugins.extract.db.parser.Table.Field;

/**
 * 
 * @author kanven
 *
 */
public class RecordRowFilter implements Filter {

	private static final Logger log = LoggerFactory.getLogger(RecordRowFilter.class);

	private Schema schema;

	public RecordRowFilter(String rule) {
		if (StringUtils.isBlank(rule)) {
			throw new RecordException("规则内容不能为空");
		}
		this.schema = SchemaParseHandler.parse(rule);
	}

	@Override
	public boolean filter(Row row) {
		if (!schema.satifyDb(row.schema())) {
			log.warn(String.format("the schema(%s) of the row not satisfy the rule", row.schema()));
			return false;
		}
		if (!schema.hasTable(row.table())) {
			log.warn(String.format("the table(%s) of the row not satisfy the rule", row.table()));
			return false;
		}
		Table table = schema.Table(row.table());
		if (table.all()) {
			return true;
		}
		if (table.hasField()) {
			Set<Column> olds = row.olds();
			Set<Column> columns = row.columns();
			if (columns != null && !columns.isEmpty()) {
				Iterator<Column> iterator = columns.iterator();
				Set<Column> cs = new HashSet<>(columns.size());
				while (iterator.hasNext()) {
					Column column = iterator.next();
					if (!table.contains(column.name())) {
						iterator.remove();
						continue;
					}
					cs.add(column);
					Field field = table.field(column.name());
					if (field.ignored()) {
						iterator.remove();
						continue;
					}
					// 移除没有修改的列
					if (olds != null && !olds.isEmpty()) {
						if (olds.contains(column)) {
							iterator.remove();
						}
					}
				}
				if (!columns.isEmpty()) {
					// 补齐合法列
					columns.addAll(cs);
				}
			}
		}
		return true;
	}

}
