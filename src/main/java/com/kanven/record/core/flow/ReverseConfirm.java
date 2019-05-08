package com.kanven.record.core.flow;

import java.io.Serializable;

/**
 * 
 * @author kanven
 *
 */
public final class ReverseConfirm implements Serializable {

	private static final long serialVersionUID = -5397277474860319114L;

	private final Long piplineId;

	private final Long processId;

	private final Long batchId;

	private final BackType type;

	public ReverseConfirm(Long piplineId, Long processId, Long batchId, BackType type) {
		this.piplineId = piplineId;
		this.processId = processId;
		this.batchId = batchId;
		this.type = type;
	}

	public final Long piplineId() {
		return this.piplineId;
	}

	public final Long processId() {
		return this.processId;
	}

	public final Long batchId() {
		return this.batchId;
	}

	public final BackType type() {
		return this.type;
	}

	@Override
	public String toString() {
		return "Back [piplineId=" + piplineId + ", processId=" + processId + ", batchId=" + batchId + ", type=" + type
				+ "]";
	}

	public static enum BackType {
		ACK, ROLLBACK;

		public boolean isAck() {
			return this.equals(ACK);
		}

		public boolean isRollback() {
			return this.equals(ROLLBACK);
		}

	}

}
