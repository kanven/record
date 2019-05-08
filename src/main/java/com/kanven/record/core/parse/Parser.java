package com.kanven.record.core.parse;

import com.alibaba.otter.canal.protocol.CanalEntry.Entry;

public interface Parser<T> {

	public T parse(Entry entry);

}
