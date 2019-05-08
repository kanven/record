package com.kanven.record.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.kanven.record.LifeCycle;
import com.kanven.record.core.fetch.CanalFetchContext;
import com.kanven.record.core.flow.FlowController;
import com.kanven.record.exception.RecordException;

/**
 * 
 * @author kanven
 *
 */
public abstract class AbstractRecord extends LifeCycle {

	protected final Long piplineId;

	protected final ExecutorService pool;

	protected final FlowController flowController;

	protected final ExecutorService executor;

	protected Valve valve;

	public AbstractRecord(Long piplineId, ExecutorService pool) {
		this.piplineId = piplineId;
		this.pool = pool;
		flowController = CanalFetchContext.getFlow(piplineId);
		if (flowController == null) {
			throw new RecordException("the flow controller of the pipline(" + piplineId + ") should not be null");
		}
		executor = Executors.newFixedThreadPool(2);
	}

	public AbstractRecord(Long piplineId, Valve valve, ExecutorService pool) {
		this(piplineId, pool);
		this.valve = valve;
		if (valve == null) {
			throw new RecordException("the valve of th pipline(" + piplineId + ") shold not be null");
		}
	}

	public Long piplineId() {
		return this.piplineId;
	}

}
