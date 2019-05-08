package com.kanven.record.core.flow;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 整合基于优先级队列，并对其元素进行去重
 * 
 * @author kanven
 *
 */
public class UniquePriorityBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E> {

	private final BlockingQueue<E> queue;

	private final Lock putLock = new ReentrantLock();

	public UniquePriorityBlockingQueue() {
		queue = new PriorityBlockingQueue<E>();
	}

	public UniquePriorityBlockingQueue(int capacity) {
		if (capacity <= 0) {
			throw new IllegalArgumentException();
		}
		queue = new PriorityBlockingQueue<E>(capacity);
	}

	public UniquePriorityBlockingQueue(int capacity, Comparator<E> comparator) {
		if (capacity <= 0) {
			throw new IllegalArgumentException();
		}
		queue = new PriorityBlockingQueue<E>(capacity, comparator);
	}

	@Override
	public boolean offer(E e) {
		if (e == null) {
			throw new NullPointerException();
		}
		putLock.lock();
		try {
			if (queue.contains(e)) {
				return true;
			}
			return queue.offer(e);
		} finally {
			putLock.unlock();
		}
	}

	@Override
	public E poll() {
		return queue.poll();
	}

	@Override
	public E peek() {
		return queue.peek();
	}

	@Override
	public Iterator<E> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return queue.size();
	}

	@Override
	public void put(E e) throws InterruptedException {
		if (e == null) {
			throw new NullPointerException();
		}
		putLock.lockInterruptibly();
		try {
			if (queue.contains(e)) {
				return;
			}
			queue.put(e);
		} finally {
			putLock.unlock();
		}
	}

	@Override
	public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
		return queue.offer(e, timeout, unit);
	}

	@Override
	public E take() throws InterruptedException {
		return queue.take();
	}

	@Override
	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		return queue.poll(timeout, unit);
	}

	@Override
	public boolean contains(Object o) {
		return queue.contains(o);
	}

	@Override
	public boolean remove(Object o) {
		return queue.remove(o);
	}

	@Override
	public void clear() {
		queue.clear();
	}

	@Override
	public int remainingCapacity() {
		return queue.remainingCapacity();
	}

	@Override
	public int drainTo(Collection<? super E> c) {
		return queue.drainTo(c);
	}

	@Override
	public int drainTo(Collection<? super E> c, int maxElements) {
		return queue.drainTo(c, maxElements);
	}

}
