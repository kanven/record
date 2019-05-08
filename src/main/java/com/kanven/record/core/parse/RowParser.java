package com.kanven.record.core.parse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.kanven.record.exception.RecordException;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.Header;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.kanven.record.core.meta.Column.ColumnBuilder;
import com.kanven.record.core.meta.Row;
import com.kanven.record.core.meta.Row.RowBuilder;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * 行数据解析
 * 
 * @author kanve
 *
 */
public class RowParser implements Parser<List<Row>> {

	public List<Row> parse(Entry entry) {
		if (entry.getEntryType() == EntryType.ROWDATA) {
			try {
				Header header = entry.getHeader();
				RowChange rc = RowChange.parseFrom(entry.getStoreValue());
				EventType eventType = rc.getEventType();
				List<RowData> rds = rc.getRowDatasList();
				List<Row> rows = new ArrayList<Row>(rds.size());
				switch (eventType) {
				case UPDATE:
					for (RowData rd : rds) {
						rows.add(parseRow(header, rd.getBeforeColumnsList(), rd.getAfterColumnsList()));
					}
					break;
				case INSERT:
					for (RowData rd : rds) {
						rows.add(parseRow(header, rd.getAfterColumnsList()));
					}
					break;
				case DELETE:
					for (RowData rd : rds) {
						rows.add(parseRow(header, rd.getBeforeColumnsList()));
					}
					break;
				default:
					throw new RecordException("unkown the event type:" + eventType);
				}
				return rows;
			} catch (InvalidProtocolBufferException e) {
				throw new RecordException("row's column value parse error,the entry origin information is:" + entry, e);
			}
		}
		if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
			return new ArrayList<>(0);
		}
		throw new RecordException("the entry type is not row data type");
	}

	private Row parseRow(Header header, List<Column> olds, List<Column> columns) {
		RowBuilder builder = RowBuilder.newBuilder();
		String schema = header.getSchemaName();
		String table = header.getTableName();
		builder.schema(schema);
		builder.table(table);
		builder.executeTime(header.getExecuteTime());
		Set<com.kanven.record.core.meta.Column> cs = new HashSet<com.kanven.record.core.meta.Column>(columns.size());
		for (Column column : columns) {
			ColumnBuilder cb = ColumnBuilder.newBuilder();
			cb.index(column.getIndex());
			cb.name(column.getName());
			cb.type(column.getMysqlType());
			cb.value(realValue(column.getMysqlType(), column.getValue()));
			boolean isKey = column.getIsKey();
			cb.key(isKey);
			if (isKey) {
				builder.primaryKey(cb.build());
			}
			cs.add(cb.build());
		}
		builder.columns(cs);
		Set<com.kanven.record.core.meta.Column> os = new HashSet<com.kanven.record.core.meta.Column>(olds.size());
		for (Column oc : olds) {
			ColumnBuilder cb = ColumnBuilder.newBuilder();
			cb.index(oc.getIndex());
			cb.name(oc.getName());
			cb.type(oc.getMysqlType());
			cb.value(realValue(oc.getMysqlType(), oc.getValue()));
			boolean isKey = oc.getIsKey();
			cb.key(isKey);
			if (isKey) {
				builder.primaryKey(cb.build());
			}
			os.add(cb.build());
		}
		builder.olds(os);
		return builder.build();
	}

	private Row parseRow(Header header, List<Column> columns) {
		RowBuilder builder = RowBuilder.newBuilder();
		String schema = header.getSchemaName();
		String table = header.getTableName();
		builder.schema(schema);
		builder.table(table);
		builder.executeTime(header.getExecuteTime());
		Set<com.kanven.record.core.meta.Column> cs = new HashSet<com.kanven.record.core.meta.Column>();
		for (Column column : columns) {
			ColumnBuilder cb = ColumnBuilder.newBuilder();
			cb.index(column.getIndex());
			cb.name(column.getName());
			cb.type(column.getMysqlType());
			cb.value(realValue(column.getMysqlType(), column.getValue()));
			boolean isKey = column.getIsKey();
			cb.key(isKey);
			if (isKey) {
				builder.primaryKey(cb.build());
			}
			cs.add(cb.build());
		}
		builder.columns(cs);
		return builder.build();
	}

	private Object realValue(String type, String value) {
		if (StringUtils.isNotBlank(value)) {
			if (type.contains("tinyint")) {
				return Integer.parseInt(value);
			}
			if (type.contains("bigint")) {
				return Long.parseLong(value);
			}
		}
		return value;
	}

}
