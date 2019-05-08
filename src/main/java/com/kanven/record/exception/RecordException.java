package com.kanven.record.exception;

public class RecordException extends RuntimeException {

	private static final long serialVersionUID = 7161041683376974020L;

	public RecordException() {
		super();
	}

	public RecordException(String message, Throwable e) {
		super(message, e);
	}

	public RecordException(String message) {
		super(message);
	}

	public RecordException(Throwable e) {
		super(e);
	}

}
