package com.kanven.record.core.domain;

import com.kanven.record.core.flow.Step;

public final class PoolRejected {

	private final Long piplineId;

	private final Long processId;

	private final Long batchId;

	private final Step step;

	public PoolRejected(Long piplineId, Long processId, Long batchId, Step step) {
		this.piplineId = piplineId;
		this.processId = processId;
		this.batchId = batchId;
		this.step = step;
	}

	public Long piplineId() {
		return this.piplineId;
	}

	public Long processId() {
		return this.processId;
	}

	public Long batchId() {
		return this.batchId;
	}

	public Step step() {
		return this.step;
	}

}
