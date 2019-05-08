package com.kanven.record.core.parse;

import com.alibaba.otter.canal.protocol.CanalEntry.Entry;

interface OperationType {

	boolean isSatisfy(Entry entry);

	Object parse(Entry entry);

}
