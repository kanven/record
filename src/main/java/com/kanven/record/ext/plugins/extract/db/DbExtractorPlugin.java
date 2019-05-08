package com.kanven.record.ext.plugins.extract.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.kanven.record.core.flow.FlowData;
import com.kanven.record.core.meta.Row;
import com.kanven.record.ext.Plugin;
import com.kanven.record.ext.plugins.extract.ExtractorPlugin;
import com.kanven.record.ext.plugins.extract.db.filter.Filter;

/**
 * 
 * @author kanven
 *
 */
@Plugin(name = "db")
public class DbExtractorPlugin implements ExtractorPlugin {

	private List<Filter> filters = new ArrayList<>();

	@Override
	public void extract(FlowData flowData) {
		List<List<Row>> rows = flowData.getRows();
		if (rows != null && !rows.isEmpty()) {
			for (List<Row> items : rows) {
				Iterator<Row> iterator = items.iterator();
				while (iterator.hasNext()) {
					Row row = iterator.next();
					if (filters.size() > 0) {
						boolean satisfy = true;
						for (Filter filter : filters) {
							if (!filter.filter(row)) {
								satisfy = false;
								iterator.remove();
								break;
							}
						}
						if (!satisfy) {
							continue;
						}
					}
					if (row.columns().isEmpty()) {
						iterator.remove();
					}
				}
			}
		}
	}

	@Override
	public void addFilter(Filter filter) {
		filters.add(filter);
	}

}
