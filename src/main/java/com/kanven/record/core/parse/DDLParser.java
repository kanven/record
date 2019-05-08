package com.kanven.record.core.parse;

import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.Header;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.google.protobuf.InvalidProtocolBufferException;
import com.kanven.record.core.meta.DDLMeta;
import com.kanven.record.core.meta.DDLMeta.DDLMetaBuilder;
import com.kanven.record.exception.RecordException;

public class DDLParser implements Parser<DDLMeta> {

	@Override
	public DDLMeta parse(Entry entry) {
		Header header = entry.getHeader();
		try {
			RowChange rc = RowChange.parseFrom(entry.getStoreValue());
			DDLMetaBuilder builder = DDLMetaBuilder.newBuilder();
			builder.schema(header.getSchemaName());
			builder.table(header.getTableName());
			builder.sql(rc.getSql());
			return builder.build();
		} catch (InvalidProtocolBufferException e) {
			throw new RecordException("ddl meta data parse error,the entry origin information is:" + entry, e);
		}
	}

}
