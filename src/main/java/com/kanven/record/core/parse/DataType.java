package com.kanven.record.core.parse;

import java.util.List;

import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.Header;
import com.kanven.record.core.meta.DDLMeta;
import com.kanven.record.core.meta.Row;

/**
 * 
 * @author kanven
 *
 */
public enum DataType implements OperationType {

	DDL(0) {

		@Override
		public boolean isSatisfy(Entry entry) {
			Header header = entry.getHeader();
			EventType eventType = header.getEventType();
			if (eventType == EventType.CREATE || eventType == EventType.ALTER || eventType == EventType.ERASE
					|| eventType == EventType.TRUNCATE || eventType == EventType.RENAME || eventType == EventType.CINDEX
					|| eventType == EventType.DINDEX) {
				return true;
			}
			return false;
		}

		@Override
		public DDLMeta parse(Entry entry) {
			return ddlParser.parse(entry);
		}

	},
	DML(1) {

		@Override
		public boolean isSatisfy(Entry entry) {
			Header header = entry.getHeader();
			EventType eventType = header.getEventType();
			if (eventType == EventType.INSERT || eventType == EventType.UPDATE || eventType == EventType.DELETE) {
				return true;
			}
			return false;
		}

		@Override
		public List<Row> parse(Entry entry) {
			return rowParser.parse(entry);
		}

	},
	QUERY(2) {

		@Override
		public boolean isSatisfy(Entry entry) {
			Header header = entry.getHeader();
			EventType eventType = header.getEventType();
			return eventType == EventType.QUERY;
		}

		@Override
		public Object parse(Entry entry) {
			// TODO Auto-generated method stub
			return null;
		}

	};

	private static Parser<DDLMeta> ddlParser = new DDLParser();

	private static Parser<List<Row>> rowParser = new RowParser();

	private int type;

	private DataType(int type) {
		this.type = type;
	}

	public int type() {
		return type;
	}

}
