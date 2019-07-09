package com.kanven.record;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import com.kanven.record.core.ThreadPoolExecutorHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kanven.record.ServerListener.ServerEvent;
import com.kanven.record.ServerListener.ServerEvent.EventType;
import com.kanven.record.core.Context;
import com.kanven.record.core.Task;
import com.kanven.record.core.domain.PoolRejected;
import com.kanven.record.core.fetch.CanalFetcher;
import com.kanven.record.core.fetch.FetcherConfig;
import com.kanven.record.exception.RecordException;
import com.kanven.record.ext.PluginContext;
import com.kanven.record.ext.plugins.alarm.AlarmPlugin;
import com.kanven.record.ext.plugins.register.ChildrenListener;
import com.kanven.record.ext.plugins.register.Register;

/**
 * 
 * @author kaven
 *
 */
public class Server extends LifeCycle implements ChildrenListener {

	private final static Logger log = LoggerFactory.getLogger(Server.class);

	private final static String SERVER_PATH_FORMAT = "/record/server/{0}";

	private final String configPath = "record.properties";

	private final String nodeIdKey = "node.id";

	private final String registerKey = "node.register";

	private final String registerPathKey = "node.register.path";

	private final String poolSizeKey = "node.pool.size";

	private final String alarmKey = "node.alarm.name";

	private final String groupKey = "node.group";

	private final String clientIdKey = "pipline.{0}.client.id";

	private final String canalIdKey = "pipline.{0}.canal.id";

	private final String clusterIdKey = "pipline.{0}.cluster.id";

	private final String slaveIdKey = "pipline.{0}.slave.id";

	private final String filterKey = "pipline.{0}.slave.filter";

	private final String destinationKey = "pipline.{0}.destination";

	private final String batchSizeKey = "pipline.{0}.batch.size";

	private final String dbHostKey = "pipline.{0}.db.host";

	private final String dbPortKey = "pipline.{0}.db.port";

	private final String dbUserNameKey = "pipline.{0}.db.username";

	private final String dbPassword = "pipline.{0}.db.password";

	private final String zkAddressKey = "pipline.{0}.zk.address";

	private final String extractorNameKey = "pipline.{0}.extractor";

	private final String extractorRuleKey = "pipline.{0}.extractor.rule";

	private final String transformNameKey = "pipline.{0}.transformer";

	private final String transformRuleKey = "pipline.{0}.transformer.rule";

	private final String loadNameKey = "pipline.{0}.loader";

	private final String loadConfigKey = "pipline.{0}.loader.config";

	private final List<CanalFetcher> fetchers = new ArrayList<>();

	private final ThreadPoolExecutorHolder holder;

	private final String id;

	private String seq;

	private final String serverPath;

	public Server() {
		Properties properties = null;
		try {
			properties = loadProperties();
		} catch (Exception e) {
			throw new RecordException("the record.properties file load failure", e);
		}
		String nodeId = properties.getProperty(nodeIdKey);
		if (StringUtils.isBlank(nodeId)) {
			throw new RecordException("the node id should not be null");
		}
		String group = properties.getProperty(groupKey, "default");
		serverPath = MessageFormat.format(SERVER_PATH_FORMAT, group);
		id = nodeId;
		int poolSize = Integer.parseInt(properties.getProperty(poolSizeKey, "10"));
		holder = new ThreadPoolExecutorHolder(poolSize, new RejectedExecutionHandler() {

			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				if (r instanceof Task) {
					Task task = (Task) r;
					ServerEvent event = new ServerEvent(id, EventType.THREAD_POOL_REJECTED,
							new PoolRejected(task.piplineId(), task.processId(), task.batchId(), task.step()));
					singleAll(event);
				}
			}

		});
		buildAlarm(properties);
		buildRegister(properties);
		loadFetcher(properties);
	}

	@Override
	public void onNotify(List<String> values) {
		if (values == null) {
			log.error("the server has't an available node");
		}
		if (values != null && !values.isEmpty()) {
			Collections.sort(values);
			String path = values.get(0);
			if (path.equals(this.seq)) {
				ServerEvent event = new ServerEvent(id, EventType.SERVER_STATUS_CHANED, true);
				singleAll(event);
				log.info("the node(" + id + ") is working");
				return;
			}
		}
		ServerEvent event = new ServerEvent(id, EventType.SERVER_STATUS_CHANED, false);
		singleAll(event);
		log.info("the node(" + id + ") is waiting");
	}

	private void singleAll(ServerEvent event) {
		for (CanalFetcher fetcher : fetchers) {
			fetcher.onNotify(event);
		}
	}

	@Override
	protected void doStart() {
		for (CanalFetcher fetcher : fetchers) {
			fetcher.start();
		}
		regist();
	}

	@Override
	protected void doStop() {
		for (CanalFetcher fetcher : fetchers) {
			try {
				fetcher.stop();
			} catch (Exception e) {
				log.error("the pipline(" + fetcher.piplineId() + ") stop failure", e);
			}
		}
		holder.shutdown();
		unregist();
		try {
			Context.register().close();
			Context.alarm().close();
		} catch (Exception e) {
			log.error("the registr close has an error", e);
		}
	}

	private Properties loadProperties() throws IOException {
		ClassLoader loader = Bootstrapper.class.getClassLoader();
		InputStream input = loader.getResourceAsStream(configPath);
		Properties properties = new Properties();
		properties.load(input);
		return properties;
	}

	private void loadFetcher(Properties properties) {
		String ids = properties.getProperty("pipline.id");
		if (StringUtils.isBlank(ids)) {
			throw new RecordException("管道编号未指定");
		}
		String[] items = ids.split(",|，");
		Set<Long> piplines = new HashSet<>(items.length);
		for (String item : items) {
			item = item.trim();
			if (StringUtils.isBlank(item)) {
				continue;
			}
			Long id = Long.parseLong(item);
			piplines.add(id);
		}
		if (piplines.isEmpty()) {
			throw new RecordException("the pipline id is not be assign");
		}
		for (Long pipline : piplines) {
			FetcherConfig config = buildConfig(properties, pipline);
			config.setPiplineId(pipline);
			Context.config(pipline, config);
			CanalFetcher fetcher = new CanalFetcher(config, holder.getExecutorService());
			fetchers.add(fetcher);
		}
	}

	private void buildAlarm(Properties properties) {
		final String name = properties.getProperty(alarmKey);
		if (StringUtils.isBlank(name)) {
			throw new RecordException("alarm should't be null");
		}
		try {
			AlarmPlugin alarmPlugin = PluginContext.getAlarm(name).newInstance();
			Context.alarm(alarmPlugin);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RecordException("the alarm create failure", e);
		}
	}

	private void buildRegister(Properties properties) {
		final String name = properties.getProperty(registerKey);
		if (StringUtils.isBlank(name)) {
			throw new RecordException("register should't be null");
		}
		String path = properties.getProperty(registerPathKey);
		try {
			Register register = null;
			if (StringUtils.isBlank(path)) {
				register = PluginContext.getRegister(name).newInstance();
			} else {
				Class<? extends Register> plugin = PluginContext.getRegister(name);
				Constructor<? extends Register> c = plugin.getConstructor(String.class);
				register = c.newInstance(path);
			}
			Context.register(register);
		} catch (Exception e) {
			throw new RecordException("the register create failure", e);
		}
	}

	private FetcherConfig buildConfig(final Properties properties, final Long pipline) {
		FetcherConfig config = new FetcherConfig();
		String clientId = properties.getProperty(MessageFormat.format(clientIdKey, pipline));
		if (StringUtils.isBlank(clientId)) {
			throw new RecordException("the pipline's client id should not be null");
		}
		config.setClientId(Integer.parseInt(clientId));
		String canalId = properties.getProperty(MessageFormat.format(canalIdKey, pipline));
		if (StringUtils.isBlank(canalId)) {
			throw new RecordException("the pipline's canal id should not be null");
		}
		config.setCanalId(Long.parseLong(canalId));
		String slaveId = properties.getProperty(MessageFormat.format(slaveIdKey, pipline));
		if (StringUtils.isBlank(slaveId)) {
			throw new RecordException("the pipline's slave id should not be null");
		}
		config.setSlaveId(Long.parseLong(slaveId));
		String clusterId = properties.getProperty(MessageFormat.format(clusterIdKey, pipline));
		if (StringUtils.isBlank(clusterId)) {
			throw new RecordException("the pipline's cluster id should not be null");
		}
		config.setClusterId(Long.parseLong(clusterId));
		config.setFilter(properties.getProperty(MessageFormat.format(filterKey, pipline), ""));
		String destination = properties.getProperty(MessageFormat.format(destinationKey, pipline));
		if (StringUtils.isBlank(destination)) {
			throw new RecordException("the pipline's destination should not be null");
		}
		config.setDestination(destination);
		config.setBatchSize(
				Integer.parseInt(properties.getProperty(MessageFormat.format(batchSizeKey, pipline), "100")));
		String host = properties.getProperty(MessageFormat.format(dbHostKey, pipline));
		if (StringUtils.isBlank(host)) {
			throw new RecordException("the pipline's mysql host should not be null");
		}
		config.setDbHost(host);
		String port = properties.getProperty(MessageFormat.format(dbPortKey, pipline), "3306");
		config.setDbPort(Integer.parseInt(port));
		config.setDbUsername(properties.getProperty(MessageFormat.format(dbUserNameKey, pipline)));
		config.setDbPassword(properties.getProperty(MessageFormat.format(dbPassword, pipline)));
		String zk = properties.getProperty(MessageFormat.format(zkAddressKey, pipline));
		if (StringUtils.isBlank(zk)) {
			throw new RecordException("the pipline's zookeeper address should not be null");
		}
		config.setZkAddress(zk);
		String extractorName = properties.getProperty(MessageFormat.format(extractorNameKey, pipline));
		if (StringUtils.isBlank(extractorName)) {
			throw new RecordException("the pipline's extractor name should not be null");
		}
		config.setExtractor(extractorName);
		String extractorRule = properties.getProperty(MessageFormat.format(extractorRuleKey, pipline));
		config.setExtractorRule(extractorRule);
		String transformName = properties.getProperty(MessageFormat.format(transformNameKey, pipline));
		if (StringUtils.isBlank(transformName)) {
			throw new RecordException("the pipline's transformer name should not be null");
		}
		String transformRule = properties.getProperty(MessageFormat.format(transformRuleKey, pipline));
		config.setTransformRule(transformRule);
		config.setTransform(transformName);
		String loadName = properties.getProperty(MessageFormat.format(loadNameKey, pipline));
		if (StringUtils.isBlank(loadName)) {
			throw new RecordException("the pipline's loader name should not be null");
		}
		config.setLoad(loadName);
		String loadConfig = properties.getProperty(MessageFormat.format(loadConfigKey, pipline));
		config.setLoadConfig(loadConfig);
		log.info("the pipline(" + pipline + ") config is:" + config);
		return config;
	}

	private void regist() {
		Register registerPlugin = Context.register();
		registerPlugin.subscribeChildrenChange(serverPath, this);
		String path = registerPlugin.createEphemeralSequential(serverPath + "/", id);
		this.seq = path.replace(serverPath + "/", "");
		log.info("the seq of the server is :" + seq);
	}

	private void unregist() {
		Register registerPlugin = Context.register();
		registerPlugin.unsubscribeChildrenChange(serverPath, this);
		registerPlugin.delete(serverPath + "/" + seq);
	}

	@Override
	public void onSession() {
		Register registerPlugin = Context.register();
		if (!registerPlugin.exist(serverPath + "/" + seq)) {
			regist();
		}
	}

}
