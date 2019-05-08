package com.kanven.record.core.jmx.mxbean;

public interface ThreadPoolExecutorMXBean {

	int getPoolSize();

	int getActiveCount();

	int getLargestPoolSize();

	int getWaitTasks();

}
