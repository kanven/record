package com.kanven.record.ext.plugins.transform;

import com.kanven.record.core.flow.FlowData;
import com.kanven.record.ext.plugins.transform.defaults.handler.Handler;

public interface TransformPlugin {

	void transform(FlowData flowData);
	
	void addHandler(Handler handler);

}
