package com.kanven.record.core.flow;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.kanven.record.core.fetch.CanalFetchContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kanven.record.LifeCycle;
import com.kanven.record.core.Context;
import com.kanven.record.core.Valve;
import com.kanven.record.ext.plugins.register.DataListener;
import com.kanven.record.ext.plugins.register.RegisterPlugin;

/**
 * 流程阀门 <br>
 * 阀门构成要素：
 * <ul>
 * <li>1.流程状态</li>
 * <li>2.ETL阶段等待</li>
 * </ul>
 * </br>
 * 
 * @author kanven
 *
 */
public class ProcessValve extends LifeCycle implements Valve, DataListener {

	private static final Logger log = LoggerFactory.getLogger(ProcessValve.class);

	private final Lock lock = new ReentrantLock();

	private final Condition condition = lock.newCondition();

	private final ThreadLocal<Long> local = new ThreadLocal<>();

	private static final String FLOW_STATUS_PATH = "/record/pipline/{0}";

	/**
	 * 启动态
	 */
	private static final int STARTED = 1;

	/**
	 * 暂停态
	 */
	private static final int PAUSE = 2;

	/**
	 * 停止态
	 */
	private static final int STOP = 3;

	private static final int INVERSE = ~STOP;

	private volatile int status = STOP;

	private final Long piplineId;

	private final String path;

	public ProcessValve(Long piplineId) {
		this.piplineId = piplineId;
		this.path = MessageFormat.format(FLOW_STATUS_PATH, piplineId);
	}

	@Override
	public Long await(Step step) throws InterruptedException {
		if (!isPass()) {
			log.info("the status of valve(1) for the pipline(" + piplineId + ") is:" + status + " and will be await");
			await();
		}
		Long processId = local.get();
		if (processId == null) {
			FlowController controller = CanalFetchContext.getFlow(piplineId);
			processId = controller.wait(step);
			log.info(String.format("the process id of the process(%s) for the pipline(" + piplineId
					+ ") get by the flow controll is : %s", step, processId));
			local.set(processId);
		}
		if (!isPass()) {
			log.info("the status of valve(2) for the pipline(" + piplineId + ") is:" + status + " and will be await");
			return await(step);
		}
		local.remove();
		log.info(String.format("the process id of the process(%s) is : %s", step, processId));
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
		return status != STOP && status != PAUSE;
	}

	@Override
	public void onNotify(List<String> values) {
		String value = values.get(0);
		Exception ex = null;
		int v = -1;
		try {
			v = Integer.parseInt(value);
		} catch (Exception e) {
			log.error("the valve has an error of the flow status change", e);
			ex = e;
		}
		if (ex == null) {
			int r = v & INVERSE;
			if (r == 0) {
				status = v;
				if (status == STARTED) {
					lock.lock();
					try {
						condition.signalAll();
					} finally {
						lock.unlock();
					}
				}
				return;
			}
		}
		status = STOP;
		changeStatus(STOP);
	}

	@Override
	protected void doStart() {
		RegisterPlugin register = Context.register();
		if (!register.exist(path)) {
			register.createPersistent(path);
		}
		Object d = register.readData(path);
		if (d == null) {
			status = STARTED;
			changeStatus(STARTED);
		} else {
			int v = Integer.parseInt((String) d);
			int r = v & INVERSE;
			if (r == 0) {
				status = v;
			} else {
				status = STARTED;
				changeStatus(status);
			}
		}
		register.subscribeDataChange(path, this);
	}

	@Override
	protected void doStop() {
		status = STOP;
		RegisterPlugin register = Context.register();
		register.unsubscribeDataChange(path, this);
		changeStatus(status);
	}

	private void changeStatus(int status) {
		RegisterPlugin register = Context.register();
		if (!register.exist(path)) {
			register.createPersistent(path);
		}
		register.writeData(path, status + "");
	}

	@Override
	public void onSession() {

	}

}
