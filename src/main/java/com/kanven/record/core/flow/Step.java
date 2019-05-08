package com.kanven.record.core.flow;

public enum Step {
	/**
	 * 拉取
	 */
	FETCH,
	/**
	 * 提取
	 */
	EXTRACT,
	/**
	 * 转换
	 */
	TRANSFORM,
	/**
	 * 加载
	 */
	LOAD;

	public boolean isFetch() {
		return this.equals(FETCH);
	}

	public boolean isExtract() {
		return this.equals(EXTRACT);
	}

	public boolean isTransform() {
		return this.equals(TRANSFORM);
	}

	public boolean isLoad() {
		return this.equals(LOAD);
	}

	public static int size() {
		return values().length;
	}

}
