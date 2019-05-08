package com.kanven.record.ext.plugins.extract;

import com.kanven.record.core.flow.FlowData;
import com.kanven.record.ext.plugins.extract.db.filter.Filter;

public interface ExtractorPlugin {

	void extract(FlowData flowData);

	void addFilter(Filter filter);

}
