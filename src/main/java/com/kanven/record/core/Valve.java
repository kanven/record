package com.kanven.record.core;

import com.kanven.record.core.flow.Step;

public interface Valve {

	public Long await(Step step) throws InterruptedException;

}
