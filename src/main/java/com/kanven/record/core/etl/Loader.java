package com.kanven.record.core.etl;

import com.kanven.record.Cycle;

public interface Loader extends Cycle {

	void load(Long processId) throws InterruptedException;

}
