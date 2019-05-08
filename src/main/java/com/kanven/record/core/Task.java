package com.kanven.record.core;

import com.kanven.record.core.flow.Step;

public abstract class Task implements Runnable {

	private final Long piplineId;

	private final Long processId;

	private final Long batchId;

	private final Step step;

	public Task(Long piplineId, Long processId, Long batchId, Step step) {
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

	@Override
	public String toString() {
		return "Task [piplineId=" + piplineId + ", processId=" + processId + ", batchId=" + batchId + ", step=" + step
				+ "]";
	}

}
