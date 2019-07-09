package com.kanven.record.core.fetch;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.canal.instance.core.CanalInstance;
import com.alibaba.otter.canal.instance.core.CanalInstanceGenerator;
import com.alibaba.otter.canal.instance.manager.CanalInstanceWithManager;
import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter.IndexMode;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter.MetaMode;
import com.alibaba.otter.canal.instance.manager.model.CanalStatus;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.ClientIdentity;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.server.embedded.CanalServerWithEmbedded;
import com.kanven.record.ServerListener;
import com.kanven.record.core.AbstractRecord;
import com.kanven.record.core.Context;
import com.kanven.record.core.Task;
import com.kanven.record.core.Valve;
import com.kanven.record.core.domain.PoolRejected;
import com.kanven.record.core.etl.Extractor;
import com.kanven.record.core.etl.Loader;
import com.kanven.record.core.etl.Transformer;
import com.kanven.record.core.etl.impl.RecordExtractor;
import com.kanven.record.core.etl.impl.RecordLoader;
import com.kanven.record.core.etl.impl.RecordTransformer;
import com.kanven.record.core.flow.FlowData;
import com.kanven.record.core.flow.ProcessValve;
import com.kanven.record.core.flow.ReverseConfirm;
import com.kanven.record.core.flow.ReverseConfirm.BackType;
import com.kanven.record.core.flow.Step;
import com.kanven.record.core.meta.DDLMeta;
import com.kanven.record.core.meta.Row;
import com.kanven.record.core.parse.DataType;
import com.kanven.record.exception.RecordException;
import com.kanven.record.ext.PluginContext;
import com.kanven.record.ext.plugins.extract.ExtractorPlugin;
import com.kanven.record.ext.plugins.extract.db.filter.Filter;
import com.kanven.record.ext.plugins.extract.db.filter.impl.RecordRowFilter;
import com.kanven.record.ext.plugins.load.LoadPlugin;
import com.kanven.record.ext.plugins.transform.TransformPlugin;
import com.kanven.record.ext.plugins.transform.defaults.handler.impl.DefaultRowHandler;

/**
 * 
 * @author kanven
 *
 */
public class CanalFetcher extends AbstractRecord implements ServerListener {

	private static final Logger log = LoggerFactory.getLogger(CanalFetcher.class);

	private CanalServerWithEmbedded embedded;

	private final FetcherConfig config;

	private final ClientIdentity identity;

	private final Extractor extractor;

	private final Transformer transformer;

	private final Loader loader;

	private final Lock lock = new ReentrantLock();

	private final Condition condition = lock.newCondition();

	private volatile boolean work = false;

	private final Timer timer;

	private final AtomicInteger count = new AtomicInteger(0);

	public CanalFetcher(FetcherConfig config, ExecutorService pool) {
		this(config.getPiplineId(), pool);
	}

	private CanalFetcher(Long piplineId, ExecutorService pool) {
		super(piplineId, pool);
		ProcessValve pv = new ProcessValve(piplineId);
		pv.start();
		this.valve = new FetcherValve(pv);
		this.config = Context.config(piplineId);
		String filter = config.getFilter();
		if (StringUtils.isNoneBlank(filter)) {
			this.identity = new ClientIdentity(config.getDestination(), (short) config.getClientId(), filter);
		} else {
			this.identity = new ClientIdentity(config.getDestination(), (short) config.getClientId());
		}
		this.extractor = new RecordExtractor(this.piplineId, valve, this.pool);
		this.transformer = new RecordTransformer(this.piplineId, valve, this.pool);
		this.loader = new RecordLoader(this.piplineId, valve, this.pool);
		this.timer = new Timer("fetcher-" + piplineId + "-monitor-timer", true);
		this.timer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (work) {
					if (count.get() == 0) {
						log.warn("the fetcher(" + piplineId + ") not fetch the data long time");
						Context.alarm().send("数据提取", "管道(" + piplineId + ")六小时内未拉取到任何数据，请及时排查并处理");
					} else {
						count.set(0);
					}
				}
			}

		}, 0, 6 * 60 * 60 * 1000);
	}

	@Override
	protected void doStart() {
		embedded = new CanalServerWithEmbedded();
		embedded.setCanalInstanceGenerator(new CanalInstanceGenerator() {

			public CanalInstance generate(String destination) {
				Canal canal = new Canal();
				canal.setId(config.getCanalId());
				canal.setStatus(CanalStatus.START);
				canal.setName(config.getDestination());
				CanalParameter parameter = new CanalParameter();
				parameter.setCanalId(config.getCanalId());
				parameter.setSlaveId(config.getSlaveId());
				parameter.setZkClusterId(config.getClusterId());
				parameter.setZkClusters(Arrays.asList(config.getZkAddress()));
				parameter.setMemoryStorageBufferSize(1024 * 16 * 2);
				parameter.setReceiveBufferSize(1024 * 16);
				InetSocketAddress address = new InetSocketAddress(config.getDbHost(), config.getDbPort());
				parameter.setDbAddresses(Arrays.asList(address));
				parameter.setDbUsername(config.getDbUsername());
				parameter.setDbPassword(config.getDbPassword());
				parameter.setMetaMode(MetaMode.MIXED);
				parameter.setIndexMode(IndexMode.ZOOKEEPER);
				parameter.setDetectingSQL("select  NOW()");
				canal.setCanalParameter(parameter);
				CanalInstanceWithManager instance = new CanalInstanceWithManager(canal, config.getFilter());
				return instance;
			}

		});
		try {
			startFlow();
		} catch (InterruptedException e) {
			throw new RecordException("the fetcher of the pipline(" + piplineId + ") start the flow failure", e);
		}
	}

	@Override
	protected void doStop() {
		work = false;
		executor.shutdownNow();
		embedded.unsubscribe(identity);
		embedded.stop(config.getDestination());
		embedded.stop();
		extractor.stop();
		transformer.stop();
		loader.stop();
		log.info("the fetcher of pipline(" + piplineId + ") is stopped");
	}

	private void startFlow() throws InterruptedException {
		startLoader();
		startTransformer();
		startExtractor();
		startAckProcess();
		startFetchProcess();
	}

	private void startFetchProcess() {
		executor.execute(new Runnable() {

			@Override
			public void run() {
				Thread.currentThread().setName("thread-" + piplineId + "-fetcher-starter");
				int total = 0;
				while (isStarted()) {
					while (!work) {
						lock.lock();
						try {
							try {
								log.info("the pipline(" + piplineId + ") is waiting.");
								condition.await();
								log.info("the pipline(" + piplineId + ") is working.");
							} catch (InterruptedException e) {
							}
						} finally {
							lock.unlock();
						}
					}
					Message message = embedded.getWithoutAck(identity, config.getBatchSize());
					final long batchId = message.getId();
					if (batchId == -1) {
						++total;
						if (total >= 500) {
							try {
								log.warn("the pipline(" + piplineId
										+ ") can't fetch the data long time and will sleep for 30 seconds");
								// 需要避免长时间空转占用CPU的情况出现,休眠30秒
								Thread.sleep(30000L);
								total = 0;
							} catch (InterruptedException e) {
							}
						}
						continue;
					}
					Long processId = null;
					try {
						processId = valve.await(Step.FETCH);
						int c = count.incrementAndGet();
						if (c == Integer.MAX_VALUE) {
							count.set(0);
						}
						doFetch(processId, message);
					} catch (Throwable e) {
						log.error(String.format("the thread(%s) interrupted", Thread.currentThread().getName()), e);
						ReverseConfirm back = new ReverseConfirm(piplineId, processId, batchId, BackType.ROLLBACK);
						flowController.rollBack(back);
					}
				}
			}

		});
		log.info("the fetcher proccess of pipline(" + piplineId + ") is started");
	}

	private void doFetch(final Long processId, final Message message) {
		final long batchId = message.getId();
		pool.execute(new Task(piplineId, processId, batchId, Step.FETCH) {

			@Override
			public void run() {
				Thread.currentThread().setName("thread-" + piplineId + "-" + processId + "-" + batchId + "-fetcher");
				try {
					FlowData flowData = new FlowData(piplineId, processId, batchId);
					flowData.setStep(Step.FETCH);
					List<Entry> entries = message.getEntries();
					List<DDLMeta> metas = new LinkedList<>();
					flowData.setDds(metas);
					List<List<Row>> rows = new LinkedList<>();
					flowData.setRows(rows);
					for (Entry entry : entries) {
						if (DataType.DDL.isSatisfy(entry)) {
							metas.add(DDLMeta.class.cast(DataType.DDL.parse(entry)));
							continue;
						}
						if (DataType.DML.isSatisfy(entry)) {
							@SuppressWarnings("unchecked")
							List<Row> rs = (List<Row>) DataType.DML.parse(entry);
							if (rs != null && !rs.isEmpty()) {
								rows.add(rs);
							}
							continue;
						}
					}
					flowController.notify(Step.EXTRACT, flowData);
				} catch (Throwable e) {
					log.error(String.format("the pipline(%s) fetch the data error from the process(%s)", piplineId,
							processId), e);
					ReverseConfirm back = new ReverseConfirm(piplineId, processId, batchId, BackType.ROLLBACK);
					flowController.rollBack(back);
				}
			}

		});
	}

	private void startAckProcess() {
		executor.execute(new Runnable() {

			@Override
			public void run() {
				Thread.currentThread().setName("thread-" + config.getDestination() + "-" + piplineId + "-ack");
				while (isStarted()) {
					ReverseConfirm confirm = null;
					try {
						confirm = flowController.waitAck();
					} catch (InterruptedException e) {
						log.error("the ack proccess of pipline(" + piplineId + ") was interrupted", e);
						continue;
					}
					if (confirm.type().isAck()) {
						try {
							embedded.ack(identity, confirm.batchId());
						} catch (Throwable e) {
							log.error(String.format("the pipline(%s) handle the ack error", piplineId), e);
							flowController.rollBack(confirm);
						}
					} else if (confirm.type().isRollback()) {
						work = false;
						embedded.rollback(identity, confirm.batchId());
						log.error(String.format("the pipline(%s) handle the ack error", piplineId));
						Context.alarm().send("数据回滚", "管道(" + piplineId + ")发生回滚操作，数据拉取操作停止，请及时排查并处理");
					} else {
						work = false;
						Context.alarm().send("数据确认类型错误", "管道(" + piplineId + ")出现未知确认类型，数据拉取操作停止，请及时排查并处理");
						throw new RecordException("the back type is not be elimited");
					}
				}
			}

		});
		log.info("the ack proccess of pipline(" + piplineId + ") is started");
	}

	private void startExtractor() {
		final String extractor = config.getExtractor();
		ExtractorPlugin plugin = CanalFetchContext.getExtractor(piplineId, extractor);
		if (plugin == null) {
			Filter filter = null;
			String rule = config.getExtractorRule();
			if (StringUtils.isNoneBlank(rule)) {
				filter = new RecordRowFilter(rule);
			}
			try {
				plugin = PluginContext.getExtractor(config.getExtractor()).newInstance();
				if (filter != null) {
					plugin.addFilter(filter);
				}
				CanalFetchContext.addExtractor(piplineId, extractor, plugin);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RecordException("the extractor plugin initial fail", e);
			}
		}
		this.extractor.start();
	}

	private void startTransformer() {
		final String name = config.getTransform();
		TransformPlugin plugin = CanalFetchContext.getTransform(piplineId, name);
		if (plugin == null) {
			try {
				plugin = PluginContext.getTransform(name).newInstance();
				DefaultRowHandler handler = new DefaultRowHandler();
				handler.schema(config.getTransformRule());
				plugin.addHandler(handler);
				CanalFetchContext.addTransform(piplineId, name, plugin);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RecordException("the transform plugin initial fail", e);
			}
		}
		this.transformer.start();
	}

	private void startLoader() {
		final String path = config.getLoadConfig();
		final String name = config.getLoad();
		LoadPlugin plugin = CanalFetchContext.getLoad(piplineId, name);
		if (plugin == null) {
			try {
				if (StringUtils.isBlank(path)) {
					plugin = PluginContext.getLoad(config.getLoad()).newInstance();
				} else {
					Class<? extends LoadPlugin> clazz = PluginContext.getLoad(name);
					Constructor<? extends LoadPlugin> c = clazz.getConstructor(String.class);
					plugin = c.newInstance(path);
				}
				CanalFetchContext.addLoad(piplineId, name, plugin);
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new RecordException("the load plugin initial fail", e);
			}
		}
		this.loader.start();
	}

	private final class FetcherValve implements Valve {

		private Valve valve;

		public FetcherValve(Valve valve) {
			this.valve = valve;
		}

		@Override
		public Long await(Step step) throws InterruptedException {
			if (!isPass()) {
				await();
			}
			Long processId = valve.await(step);
			if (!isPass()) {
				await();
			}
			return processId;
		}

		private void await() throws InterruptedException {
			lock.lockInterruptibly();
			try {
				condition.await();
			} finally {
				lock.unlock();
			}
		}

		private boolean isPass() {
			return work;
		}

	}

	@Override
	public void onNotify(ServerEvent event) {
		Object data = event.getData();
		switch (event.type()) {
		case SERVER_STATUS_CHANED:
			boolean current = (boolean) data;
			if (current == work) {
				return;
			}
			work = current;
			if (work) {
				log.info("canal fetcher(" + piplineId + ") data start");
				embedded.start();
				embedded.start(config.getDestination());
				embedded.subscribe(identity);
				lock.lock();
				try {
					condition.signalAll();
				} finally {
					lock.unlock();
				}
			} else {
				log.warn("canal fetcher(" + piplineId + ") data stop");
				embedded.unsubscribe(identity);
				embedded.stop(config.getDestination());
				embedded.stop();
				Context.alarm().send("数据拉取停止", "管道(" + piplineId + ")工作停止，请及时排查并处理");
			}
			break;
		case THREAD_POOL_REJECTED:
			PoolRejected rejected = (PoolRejected) data;
			if (piplineId.equals(rejected.piplineId())) {
				ReverseConfirm back = new ReverseConfirm(rejected.piplineId(), rejected.processId(), rejected.batchId(),
						BackType.ROLLBACK);
				work = false;
				flowController.rollBack(back);
			}
			break;
		}

	}

}
