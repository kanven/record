package com.kanven.record.ext.plugins.extract.db.filter.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.kanven.record.core.meta.Column;
import com.kanven.record.core.meta.Row;
import com.kanven.record.ext.plugins.extract.db.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kanven.record.ext.plugins.extract.db.Rule;

/**
 * 
 * @author kanven
 *
 */
public class RecordRowFilter implements Filter {

	private static final Logger log = LoggerFactory.getLogger(RecordRowFilter.class);

	private Rule rule;

	public RecordRowFilter(Rule rule) {
		this.rule = rule;
	}

	@Override
	public boolean filter(Row row) {
		if (!rule.satifyDb(row.schema())) {
			log.warn(String.format("the schema(%s) of the row not satisfy the rule", row.schema()));
			return false;
		}
		if (!rule.hasTable(row.table())) {
			log.warn(String.format("the table(%s) of the row not satisfy the rule", row.table()));
			return false;
		}
		Set<String> fields = rule.getColumns(row.table());
		if (fields != null && !fields.isEmpty()) {
			Set<Column> olds = row.olds();
			Set<Column> columns = row.columns();
			if (columns != null && !columns.isEmpty()) {
				Iterator<Column> iterator = columns.iterator();
				Set<Column> cs = new HashSet<>(columns.size());
				while (iterator.hasNext()) {
					Column column = iterator.next();
					if (!fields.contains(column.name())) {
						iterator.remove();
						continue;
					}
					cs.add(column);
					if (rule.isIgnore(row.table(), column.name())) {
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
