package com.kanven.record.core.fetch;

import java.io.Serializable;

/**
 * 
 * @author kanven
 *
 */
public class FetcherConfig implements Serializable {

	private static final long serialVersionUID = -8398435717655219594L;

	private int clientId;

	private long canalId;

	private long slaveId;

	private String destination;

	private String dbHost;

	private int dbPort;

	private String dbUsername;

	private String dbPassword;

	private long clusterId;

	private String zkAddress;

	private String filter;

	private int batchSize;

	private long timeout;

	private Long piplineId;

	private int parallelism;

	private String extractor;

	private String extractorRule;

	private String transform = "default";

	private String transformRule;

	private String load;

	private String loadConfig;

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}

	public long getCanalId() {
		return canalId;
	}

	public void setCanalId(long canalId) {
		this.canalId = canalId;
	}

	public long getSlaveId() {
		return slaveId;
	}

	public void setSlaveId(long slaveId) {
		this.slaveId = slaveId;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getDbHost() {
		return dbHost;
	}

	public void setDbHost(String dbHost) {
		this.dbHost = dbHost;
	}

	public int getDbPort() {
		return dbPort;
	}

	public void setDbPort(int dbPort) {
		this.dbPort = dbPort;
	}

	public String getDbUsername() {
		return dbUsername;
	}

	public void setDbUsername(String dbUsername) {
		this.dbUsername = dbUsername;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public long getClusterId() {
		return clusterId;
	}

	public void setClusterId(long clusterId) {
		this.clusterId = clusterId;
	}

	public String getZkAddress() {
		return zkAddress;
	}

	public void setZkAddress(String zkAddress) {
		this.zkAddress = zkAddress;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public Long getPiplineId() {
		return piplineId;
	}

	public void setPiplineId(Long piplineId) {
		this.piplineId = piplineId;
	}

	public int getParallelism() {
		return parallelism;
	}

	public void setParallelism(int parallelism) {
		this.parallelism = parallelism;
	}

	public String getExtractor() {
		return extractor;
	}

	public void setExtractor(String extractor) {
		this.extractor = extractor;
	}

	public String getExtractorRule() {
		return extractorRule;
	}

	public void setExtractorRule(String extractorRule) {
		this.extractorRule = extractorRule;
	}

	public String getTransform() {
		return transform;
	}

	public void setTransform(String transform) {
		this.transform = transform;
	}

	public String getTransformRule() {
		return transformRule;
	}

	public void setTransformRule(String transformRule) {
		this.transformRule = transformRule;
	}

	public String getLoad() {
		return load;
	}

	public void setLoad(String load) {
		this.load = load;
	}

	public String getLoadConfig() {
		return loadConfig;
	}

	public void setLoadConfig(String loadConfig) {
		this.loadConfig = loadConfig;
	}

	@Override
	public String toString() {
		return "FetcherConfig [clientId=" + clientId + ", canalId=" + canalId + ", slaveId=" + slaveId
				+ ", destination=" + destination + ", dbHost=" + dbHost + ", dbPort=" + dbPort + ", dbUsername="
				+ dbUsername + ", dbPassword=" + dbPassword + ", clusterId=" + clusterId + ", zkAddress=" + zkAddress
				+ ", filter=" + filter + ", batchSize=" + batchSize + ", timeout=" + timeout + ", piplineId="
				+ piplineId + ", parallelism=" + parallelism + ", extractor=" + extractor + ", extractorRule="
				+ extractorRule + ", transform=" + transform + ", transformRule=" + transformRule + ", load=" + load
				+ ", loadConfig=" + loadConfig + "]";
	}

}
