package com.kanven.record.ext.plugins.transform.defaults;

import java.util.ArrayList;
import java.util.List;

import com.kanven.record.core.flow.FlowData;
import com.kanven.record.core.meta.Row;
import com.kanven.record.ext.Plugin;
import com.kanven.record.ext.plugins.transform.Transform;
import com.kanven.record.ext.plugins.transform.defaults.handler.Handler;

@Plugin(name = "default")
public class DefaultTransform implements Transform {
	
	private List<Handler> handlers = new ArrayList<>();

	@Override
	public void transform(FlowData flowData) {
		List<List<Row>> rows = flowData.getRows();
		if(rows == null || rows.isEmpty()){
			return;
		}
		for(List<Row> rs : rows){
			if(rs == null){
				continue;
			}
			for(Row row : rs){
				for (Handler handler : handlers) {
					handler.handle(row);
				}
			}
		}
	}
	
	public void addHandler(Handler handler){
		handlers.add(handler);
	}

}
