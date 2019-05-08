package com.kanven.record.core.etl.impl;

import java.util.concurrent.ExecutorService;

import com.kanven.record.core.etl.Transformer;
import com.kanven.record.core.fetch.CanalFetchContext;
import com.kanven.record.core.fetch.FetcherConfig;
import com.kanven.record.core.flow.FlowData;
import com.kanven.record.core.flow.ReverseConfirm;
import com.kanven.record.core.flow.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kanven.record.core.AbstractRecord;
import com.kanven.record.core.Context;
import com.kanven.record.core.Task;
import com.kanven.record.core.Valve;

/**
 * 
 * @author kanven
 *
 */
public final class RecordTransformer extends AbstractRecord implements Transformer {

	private static final Logger log = LoggerFactory.getLogger(RecordTransformer.class);

	public RecordTransformer(Long piplineId, Valve valve, ExecutorService pool) {
		super(piplineId, valve, pool);
	}

	@Override
	public void transform(final Long processId) {
		final FlowData flowData = flowController.getProccessData(processId);
		if(flowData == null){
			log.warn("the data of the process("+processId+") in the  transform step is null");
			return;
		}
		pool.execute(new Task(piplineId, processId, flowData.getBatchId(), Step.TRANSFORM) {

			@Override
			public void run() {
				Thread.currentThread().setName("thread-" + piplineId + "-" + processId + "-transformer");
				try {
					FetcherConfig config = Context.config(piplineId);
					CanalFetchContext.getTransform(piplineId, config.getTransform()).transform(flowData);
					flowController.notify(Step.LOAD, flowData);
				} catch (Exception e) {
					log.error(String.format("the pipline(%s)'s transformer(%s) handle failue", piplineId, processId),
							e);
					flowController.rollBack(
							new ReverseConfirm(piplineId, processId, flowData.getBatchId(), ReverseConfirm.BackType.ROLLBACK));
				}
			}

		});

	}

	@Override
	protected void doStart() {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("thread-" + piplineId + "-transformer-starter");
				while (isStarted()) {
					try {
						Long processId = valve.await(Step.TRANSFORM);
						transform(processId);
					} catch (Throwable e) {
						log.error(String.format(
								"the thread of the pipline(%s) which start the transformer is be interrupted",
								piplineId), e);
						Context.alarm().send("数据转换异常", "管道(" + piplineId + ")获取转换数据流程编号出现异常，请及时排查并处理");
					}
				}
			}
		});
		log.info("the transformer of pipline(" + piplineId + ") is started");
	}

	@Override
	protected void doStop() {
		executor.shutdownNow();
		log.info("the transformer of pipline(" + piplineId + ") is stopped");
	}

}
