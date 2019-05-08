package com.kanven.record.core.etl;

import com.kanven.record.Cycle;

public interface Transformer extends Cycle {

	void transform(Long processId);

}
