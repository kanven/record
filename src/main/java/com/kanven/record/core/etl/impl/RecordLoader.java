package com.kanven.record.core.etl.impl;

import java.util.concurrent.ExecutorService;

import com.kanven.record.core.fetch.CanalFetchContext;
import com.kanven.record.core.fetch.FetcherConfig;
import com.kanven.record.core.flow.FlowData;
import com.kanven.record.core.flow.ReverseConfirm;
import com.kanven.record.core.flow.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kanven.record.core.AbstractRecord;
import com.kanven.record.core.Context;
import com.kanven.record.core.Valve;
import com.kanven.record.core.etl.Loader;
import com.kanven.record.ext.plugins.load.Load;

/**
 * 
 * @author kanven
 *
 */
public final class RecordLoader extends AbstractRecord implements Loader {

	private static final Logger log = LoggerFactory.getLogger(RecordLoader.class);

	private final Runnable task = new Runnable() {
		@Override
		public void run() {
			while (isStarted()) {
				try {
					Long processId = valve.await(Step.LOAD);
					Thread.currentThread().setName("thread-" + piplineId + "-" + processId + "-loader");
					load(processId);
				} catch (Throwable e) {
					log.error(String.format("the thread of the pipline(%s) which start the loader is be interrupted",
							piplineId), e);
					Context.alarm().send("数据加载异常", "管道(" + piplineId + ")获取加载数据流程编号出现异常，请及时排查并处理");
				}
			}
		}
	};

	public RecordLoader(Long piplineId, Valve valve, ExecutorService pool) {
		super(piplineId, valve, pool);
	}

	@Override
	public void load(Long processId) throws InterruptedException {
		FlowData flowData = null;
		try {
			flowData = flowController.getProccessData(processId);
			if (flowData == null) {
				log.warn("the data of the process(" + processId + ") in the  load step is null");
				return;
			}
			doLoader(flowData);
			flowController.notify(Step.FETCH, flowData);
		} catch (Exception e) {
			log.error(String.format("the pipline(%s)'s loader(%s) handle failue", piplineId, processId), e);
			flowController.rollBack(new ReverseConfirm(piplineId, processId, flowData.getBatchId(), ReverseConfirm.BackType.ROLLBACK));
		}
	}

	private void doLoader(FlowData flowData) {
		FetcherConfig config = Context.config(piplineId);
		Load plugin = CanalFetchContext.getLoad(piplineId, config.getLoad());
		plugin.load(flowData);
	}

	@Override
	protected void doStart() {
		executor.execute(task);
		log.info("the loader task of pipline(" + piplineId + ") is start");
	}

	@Override
	protected void doStop() {
		executor.shutdownNow();
		FetcherConfig config = Context.config(piplineId);
		Load plugin = CanalFetchContext.getLoad(piplineId, config.getLoad());
		plugin.close();
		log.info("the loader of pipline(" + piplineId + ") is stopped");
	}

}
