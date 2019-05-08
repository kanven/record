package com.kanven.record.core.etl;

import com.kanven.record.Cycle;

public interface Extractor extends Cycle {

	void extract(Long processId);

}
