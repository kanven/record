package com.kanven.record.ext.plugins.load.es;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

class Cache {

	private ConcurrentMap<String, Value<?>> cache = new ConcurrentHashMap<>();

	public <T> void set(String key, T value, long timeout, TimeUnit unit) {
		cache.put(key, new Value<T>(value, timeout, unit));
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		Value<?> v = cache.get(key);
		if (v != null) {
			long now = v.unit.toNanos(System.currentTimeMillis());
			if (now < v.expired) {
				return (T) v.value;
			}
			cache.remove(key, v);
		}
		return null;
	}

	public boolean hasKey(String key) {
		Object v = get(key);
		return v == null ? false : true;
	}

	private static class Value<T> {

		private T value;

		private TimeUnit unit;

		private long expired;

		public Value(T value, long timeout, TimeUnit unit) {
			this.value = value;
			this.unit = unit;
			if (timeout > 0) {
				expired = unit.toNanos(timeout) + unit.toNanos(System.currentTimeMillis());
			} else {
				expired = -1;
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (expired ^ (expired >>> 32));
			result = prime * result + ((unit == null) ? 0 : unit.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Value<?> other = (Value<?>) obj;
			if (expired != other.expired)
				return false;
			if (unit != other.unit)
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

	}

}
