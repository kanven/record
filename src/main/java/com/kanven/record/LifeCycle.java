package com.kanven.record;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class LifeCycle implements Cycle {

	private final AtomicBoolean started = new AtomicBoolean(false);

	public void start() {
		if (started.compareAndSet(false, true)) {
			doStart();
		}
	}

	public void stop() {
		if (started.compareAndSet(true, false)) {
			doStop();
		}
	}

	public boolean isStarted() {
		return started.get();
	}

	protected abstract void doStart();

	protected abstract void doStop();

}
