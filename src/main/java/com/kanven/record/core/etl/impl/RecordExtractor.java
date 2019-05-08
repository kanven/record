package com.kanven.record.core.etl.impl;

import java.util.concurrent.ExecutorService;

import com.kanven.record.core.etl.Extractor;
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
import com.kanven.record.ext.plugins.extract.ExtractorPlugin;

/**
 * 
 * @author kanven
 *
 */
public final class RecordExtractor extends AbstractRecord implements Extractor {

	private static final Logger log = LoggerFactory.getLogger(RecordExtractor.class);

	public RecordExtractor(Long piplineId, Valve valve, ExecutorService pool) {
		super(piplineId, valve, pool);
	}

	@Override
	public void extract(final Long processId) {
		final FlowData flowData = flowController.getProccessData(processId);
		if(flowData == null){
			log.warn("the data of the process("+processId+") in the  extract step is null");
			return;
		}
		pool.execute(new Task(processId, processId, flowData.getBatchId(), Step.EXTRACT) {

			@Override
			public void run() {
				Thread.currentThread().setName("thread-" + piplineId + "-" + processId + "-extractor");
				Exception ex = null;
				try {
					FetcherConfig config = Context.config(piplineId);
					ExtractorPlugin plugin = CanalFetchContext.getExtractor(piplineId, config.getExtractor());
					plugin.extract(flowData);
					flowController.notify(Step.TRANSFORM, flowData);
				} catch (InterruptedException e) {
					ex = e;
				} catch (Exception e) {
					ex = e;
				}
				if (ex != null) {
					log.error(String.format("the pipline(%s)'s extractor(%s) handle failue", piplineId, processId), ex);
					ReverseConfirm back = new ReverseConfirm(piplineId, processId, flowData.getBatchId(),
							ReverseConfirm.BackType.ROLLBACK);
					flowController.rollBack(back);
				}
			}

		});
	}

	@Override
	protected void doStart() {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("thread-" + piplineId + "-extractor-starter");
				while (isStarted()) {
					try {
						final Long processId = valve.await(Step.EXTRACT);
						extract(processId);
					} catch (Throwable e) {
						log.error(String.format(
								"the thread of the pipline(%s) which start the extractor is be interrupted", piplineId),
								e);
						Context.alarm().send("数据提取异常", "管道(" + piplineId + ")获取提取数据流程编号出现异常，请及时排查并处理");
					}
				}
			}
		});
		log.info("the extractor of pipline(" + piplineId + ") is started");
	}

	@Override
	protected void doStop() {
		executor.shutdownNow();
		log.info("the extractor of pipline(" + piplineId + ") is stopped");
	}

}
