package com.kanven.record.ext.plugins.load;

import com.kanven.record.core.flow.FlowData;

public interface Load {

	void load(FlowData flowData);

	void close();

}
