package com.kanven.record.core.flow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.kanven.record.exception.RecordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kanven.record.core.Constants;

/**
 * 流程流转控制器
 * 
 * @author kanven
 *
 */
public class FlowController {

	private static final Logger log = LoggerFactory.getLogger(FlowController.class);

	private ConcurrentMap<Step, BlockingQueue<Long>> steps = new ConcurrentHashMap<Step, BlockingQueue<Long>>(
			Step.size());

	private Map<Long, FlowData> processes = new HashMap<Long, FlowData>();

	private BlockingQueue<ReverseConfirm> backs = new LinkedBlockingQueue<ReverseConfirm>();

	private AtomicLong processNumber = new AtomicLong(0);

	private int parallelism = Constants.DEFAULT_PARALLELISM;

	private Lock loadLock = new ReentrantLock();

	private Lock processLock = new ReentrantLock();

	private Condition empty = processLock.newCondition();

	public FlowController() {

	}

	public FlowController(int parallelism) {
		if (parallelism > 0) {
			this.parallelism = parallelism;
		}
	}

	/**
	 * <br>
	 * 获取流程对应阶段processId </br>
	 * <ul>
	 * <li>1、fetch：启动阶段，需要初始化，后续每处理完一个process，新增一个；</li>
	 * <li>2、load：最小process优先处理，如果不是最小process，则需要等待；</li>
	 * </ul>
	 * 
	 * @param step
	 * @return
	 * @throws InterruptedException
	 */
	public Long wait(Step step) throws InterruptedException {
		/**
		 * 初始化数据拉取
		 */
		if (step.isFetch() && !steps.containsKey(step)) {
			initFetch();
		}
		if (step.isLoad()) {
			while (true) {
				/**
				 * 通过流程判断是否可以执行当前最小load任务
				 */
				BlockingQueue<Long> queue = getStepQueue(step, parallelism);
				Long process = queue.take();
				FlowData flow = processes.get(process);
				if (flow == null) {
					log.warn("the load step has remove from the proccess(" + process + ")");
					continue;
				}
				if (flow.getStep().isLoad()) {
					log.info("the load task(" + flow.getPiplineId() + ":" + flow.getProcessId()
							+ ") start,and the number of the load task is :" + queue.size());
					return process;
				}
			}
		}
		BlockingQueue<Long> queue = getStepQueue(step, parallelism);
		Long process = queue.take();
		log.info(String.format("wait:the proceess(%s) is in step:%s", process, step));
		return process;
	}

	public synchronized void notify(Step step, FlowData d) throws InterruptedException {
		// 兼容roll back下process被清理场景
		if (!processes.containsKey(d.getProcessId())) {
			log.warn(String.format("the pipline(%s) of the process(%s) not exist", d.getPiplineId(), d.getProcessId()));
			return;
		}
		log.info(String.format("notify:the process(%s) of pipline(%s) is in step:%s", d.getProcessId(),
				d.getPiplineId(), step));
		switch (step) {
		case EXTRACT:
		case TRANSFORM:
			d.setStep(step);
			processes.put(d.getProcessId(), d);
			BlockingQueue<Long> queue = getStepQueue(step, parallelism);
			queue.put(d.getProcessId());
			break;
		case LOAD:
			// load阶段需要确保有序串行进行
			d.setStep(step);
			processes.put(d.getProcessId(), d);
			nextLoad();
			break;
		case FETCH:
			Long processId = d.getProcessId();
			ReverseConfirm ack = new ReverseConfirm(d.getPiplineId(), processId, d.getBatchId(), ReverseConfirm.BackType.ACK);
			backs.put(ack);
			processes.remove(processId);
			nextLoad();
			// 避免多个load导致在process未移除之前重新塞回step
			steps.get(Step.LOAD).remove(processId);
			nextProcess(d.getPiplineId(), processId);
			break;
		default:
			throw new IllegalArgumentException("unsupport flow step:" + step);
		}

	}

	public FlowData getProccessData(Long processId) {
		return processes.get(processId);
	}

	private void initFetch() throws InterruptedException {
		initFetch(parallelism);
	}

	private void initFetch(int n) throws InterruptedException {
		BlockingQueue<Long> queue = getStepQueue(Step.FETCH, n);
		for (int i = 1; i <= n; i++) {
			long process = processNumber.incrementAndGet();
			queue.put(process);
			processes.put(process, new FlowData(process));
		}
	}

	private BlockingQueue<Long> getStepQueue(Step step, int n) {
		BlockingQueue<Long> queue = steps.get(step);
		if (queue == null) {
			log.warn("the queue of the step(" + step + ") created");
			steps.putIfAbsent(step, new UniquePriorityBlockingQueue<Long>(n));
			queue = steps.get(step);
		}
		return queue;
	}

	private void nextProcess(long pipline, long currentProcess) throws InterruptedException {
		// 处理循环问题
		if (processNumber.get() == Long.MAX_VALUE) {
			processLock.lockInterruptibly();
			try {
				log.info("the process id is already up to the max number");
				if (processes.isEmpty()) {
					processNumber.set(0);
					empty.signalAll();
				} else {
					empty.await();
				}
			} finally {
				processLock.unlock();
			}
		}
		Long nextProcess = processNumber.incrementAndGet();
		log.info(String.format("the current process of pipline(%s) is %s and the next is %s", pipline, currentProcess,
				nextProcess));
		steps.get(Step.FETCH).put(nextProcess);
		processes.put(nextProcess, new FlowData(nextProcess));
	}

	private void nextLoad() throws InterruptedException {
		loadLock.lockInterruptibly();
		try {
			Long minProcess = getMinProcess();
			if (minProcess != null) {
				log.info("the min process id is:" + minProcess);
				getStepQueue(Step.LOAD, parallelism).put(minProcess);
			}
		} finally {
			loadLock.unlock();
		}
	}

	/**
	 * 获取最小处理过程序号
	 * 
	 * @return 最小序号值
	 */
	private Long getMinProcess() {
		Set<Long> keys = processes.keySet();
		if (keys == null || keys.isEmpty()) {
			return null;
		}
		return Collections.min(keys);
	}

	public ReverseConfirm waitAck() throws InterruptedException {
		return backs.take();
	}

	/**
	 * 异常流程回滚
	 */
	public synchronized void rollBack(ReverseConfirm confirm) {
		if (confirm.type().isRollback()) {
			Long processId = confirm.processId();
			if (!processes.containsKey(processId)) {
				log.warn("the process(" + processId + ")is not exist");
				return;
			}
			List<Long> processIds = new ArrayList<>(processes.keySet());
			Collections.sort(processIds);
			int num = 0;
			for (Long v : processIds) {
				if (v >= processId) {
					processes.remove(v);
					log.info("the process(" + v + ") was be removed from the map");
					num++;
				}
			}
			log.warn(String.format(
					"the proccess(%s) casuse an error,should be rollback and the bigger processes also be done too,the number of the processes is:%s",
					processId, num));
			try {
				initFetch(num);
			} catch (InterruptedException e) {
				log.error("init the processes has an error");
			}
			backs.offer(confirm);
			return;
		}
		throw new RecordException("the back information is not rollback type");
	}

}
