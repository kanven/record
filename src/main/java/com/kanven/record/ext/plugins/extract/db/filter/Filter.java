package com.kanven.record.ext.plugins.extract.db.filter;

import com.kanven.record.core.meta.Row;

public interface Filter {

	boolean filter(Row row);

}
