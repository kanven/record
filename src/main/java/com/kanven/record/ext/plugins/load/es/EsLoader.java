package com.kanven.record.ext.plugins.load.es;

import java.util.Properties;

import com.kanven.record.core.flow.FlowData;
import com.kanven.record.ext.plugins.load.Load;
import org.apache.commons.lang3.StringUtils;

import com.kanven.record.ext.PluginConfigUtil;
import com.kanven.record.ext.Plugin;

/**
 * 
 * @author kanven
 *
 */
@Plugin(name = "es")
public class EsLoader implements Load {

	private static final String DEFAULT_CONFIG_PATH = "ext/load/es/es.properties";

	private EsClient client;

	public EsLoader(String configPath) {
		if (StringUtils.isBlank(configPath)) {
			configPath = DEFAULT_CONFIG_PATH;
		}
		Properties properties = PluginConfigUtil.config(configPath);
		String name = properties.getProperty("es.cluster.name");
		String address = properties.getProperty("es.cluster.address");
		client = new EsClient(name, address);
	}

	public EsLoader() {
		this(DEFAULT_CONFIG_PATH);
	}

	@Override
	public void load(FlowData flowData) {
		client.record(flowData);
	}

	@Override
	public void close() {
		client.close();
	}

}
