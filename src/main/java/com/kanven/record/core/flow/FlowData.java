package com.kanven.record.core.flow;

import java.io.Serializable;
import java.util.List;

import com.kanven.record.core.meta.DDLMeta;
import com.kanven.record.core.meta.Row;

/**
 * 
 * @author kanven
 *
 */
public class FlowData implements Serializable {

	private static final long serialVersionUID = -4686565476669033734L;

	private Long piplineId;

	private Long processId;

	private Step step;

	private Long batchId;

	private List<List<Row>> rows;

	private List<DDLMeta> dds;

	public FlowData(Long processId) {
		this.processId = processId;
		this.step = Step.FETCH;
	}

	public FlowData(Long piplineId, Long processId, Long batchId) {
		this.piplineId = piplineId;
		this.processId = processId;
		this.batchId = batchId;
	}

	public Long getPiplineId() {
		return piplineId;
	}

	public void setPiplineId(Long piplineId) {
		this.piplineId = piplineId;
	}

	public Long getProcessId() {
		return processId;
	}

	public void setProcessId(Long processId) {
		this.processId = processId;
	}

	public Step getStep() {
		return step;
	}

	public void setStep(Step step) {
		this.step = step;
	}

	public Long getBatchId() {
		return batchId;
	}

	public void setBatchId(Long batchId) {
		this.batchId = batchId;
	}

	public List<List<Row>> getRows() {
		return rows;
	}

	public void setRows(List<List<Row>> rows) {
		this.rows = rows;
	}

	public List<DDLMeta> getDds() {
		return dds;
	}

	public void setDds(List<DDLMeta> dds) {
		this.dds = dds;
	}

	@Override
	public String toString() {
		return "FlowData [piplineId=" + piplineId + ", processId=" + processId + ", step=" + step + ", batchId="
				+ batchId + ", rows=" + rows + ", dds=" + dds + "]";
	}

}
