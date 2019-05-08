package com.kanven.record.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.kanven.record.core.jmx.JMXExport;
import com.kanven.record.core.jmx.mxbean.ThreadPoolExecutorMXBean;

/**
 * 
 * @author kanven
 *
 */
public class ThreadPoolExecutorHolder implements ThreadPoolExecutorMXBean {

	private ThreadPoolExecutor executor;

	public ThreadPoolExecutorHolder(int threads, int capacity, ThreadFactory threadFactory,
			RejectedExecutionHandler handler) {
		executor = new ThreadPoolExecutor(threads, threads, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(capacity), threadFactory, handler);
		JMXExport.getInstance().export("record-thread-pool", this);
	}

	public ThreadPoolExecutorHolder(int threads, int capacity, RejectedExecutionHandler handler) {
		this(threads, capacity, Executors.defaultThreadFactory(), handler);
	}

	public ThreadPoolExecutorHolder(int threads, RejectedExecutionHandler handler) {
		this(threads, Integer.MAX_VALUE, handler);
	}
	
	public ExecutorService getExecutorService() {
		return executor;
	}

	@Override
	public int getPoolSize() {
		return executor.getPoolSize();
	}

	@Override
	public int getActiveCount() {
		return executor.getActiveCount();
	}

	@Override
	public int getLargestPoolSize() {
		return executor.getLargestPoolSize();
	}

	public void shutdown() {
		executor.shutdown();
	}

	@Override
	public int getWaitTasks() {
		BlockingQueue<Runnable> waiters = executor.getQueue();
		if (waiters == null) {
			return 0;
		}
		return waiters.size();
	}

}
