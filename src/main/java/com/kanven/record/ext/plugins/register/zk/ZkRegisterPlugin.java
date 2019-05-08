package com.kanven.record.ext.plugins.register.zk;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import com.kanven.record.exception.RecordException;
import com.kanven.record.ext.plugins.register.ChildrenListener;
import com.kanven.record.ext.plugins.register.DataListener;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kanven.record.ext.Plugin;
import com.kanven.record.ext.PluginConfigUtil;
import com.kanven.record.ext.plugins.register.RegisterPlugin;

/**
 * 
 * @author kanven
 *
 */
@Plugin(name = "zk")
public class ZkRegisterPlugin implements RegisterPlugin {

	private static final Logger log = LoggerFactory.getLogger(ZkRegisterPlugin.class);

	private static final String DEFAULT_REGISTER_CONFIG_PATH = "ext/register/zk/zk.properties";

	private ConcurrentMap<String, ChildrenListener> listeners = new ConcurrentHashMap<>();

	private ConcurrentMap<String, DataListener> dataListeners = new ConcurrentHashMap<>();

	private ReentrantLock lock = new ReentrantLock();

	private ZkClient client;

	public ZkRegisterPlugin() {
		this(DEFAULT_REGISTER_CONFIG_PATH);
	}

	public ZkRegisterPlugin(String configPath) {
		if (StringUtils.isBlank(configPath)) {
			configPath = DEFAULT_REGISTER_CONFIG_PATH;
		}
		Properties properties = PluginConfigUtil.config(configPath);
		String address = properties.getProperty("zk.address");
		if (StringUtils.isBlank(address)) {
			throw new RecordException("the address of zk should not be null");
		}
		int connTimeout = Integer.parseInt(properties.getProperty("zk.conn.timeout", "3000"));
		int sessionTimeout = Integer.parseInt(properties.getProperty("zk.session.timeout", "3000"));
		init(address, sessionTimeout, connTimeout);
	}

	private void init(String conn, int sessionTimeout, int connectionTimeout) {
		client = new ZkClient(conn, sessionTimeout, connectionTimeout);
		client.subscribeStateChanges(new IZkStateListener() {

			@Override
			public void handleStateChanged(KeeperState state) throws Exception {
			}

			@Override
			public void handleSessionEstablishmentError(Throwable error) throws Exception {
			}

			@Override
			public void handleNewSession() throws Exception {
				lock.lock();
				try {
					for (String key : listeners.keySet()) {
						try {
							listeners.get(key).onSession();
						} catch (Exception e) {
							log.error("the children listener(" + key + ") create a new session failure", e);
						}
					}
					for (String key : dataListeners.keySet()) {
						try {
							dataListeners.get(key).onSession();
						} catch (Exception e) {
							log.error("the data listener(" + key + ") create a new session failure", e);
						}
					}
				} finally {
					lock.unlock();
				}
			}
		});
	}

	public void close() throws Exception {
		if (client != null) {
			client.close();
		}
		listeners.clear();
		dataListeners.clear();
	}

	@Override
	public void subscribeChildrenChange(String path, ChildrenListener listener) {
		if (StringUtils.isBlank(path) || listener == null) {
			throw new IllegalArgumentException("监听路径或监听器为空！");
		}
		String p = format(path);
		if (!client.exists(p)) {
			client.createPersistent(p, true);
		}
		client.subscribeChildChanges(path, new IZkChildListener() {
			@Override
			public void handleChildChange(String path, List<String> childs) throws Exception {
				ChildrenListener listener = listeners.get(path);
				if (listener != null) {
					listener.onNotify(childs);
				}
			}
		});
		listeners.putIfAbsent(p, listener);
	}

	@Override
	public void subscribeDataChange(String path, DataListener listener) {
		if (StringUtils.isBlank(path) || listener == null) {
			throw new IllegalArgumentException("监听路径或监听器为空！");
		}
		String p = format(path);
		if (!client.exists(p)) {
			client.createPersistent(p, true);
		}
		client.subscribeDataChanges(path, new IZkDataListener() {

			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
			}

			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
				ChildrenListener listener = dataListeners.get(dataPath);
				if (listener != null) {
					List<String> values = new ArrayList<>();
					values.add((String) data);
					listener.onNotify(values);
				}
			}
		});
		dataListeners.putIfAbsent(p, listener);
	}

	@Override
	public void unsubscribeDataChange(String path, DataListener listener) {
		if (StringUtils.isBlank(path) || listener == null) {
			throw new IllegalArgumentException("监听路径或监听器为空！");
		}
		String p = format(path);
		dataListeners.remove(p, listener);
	}

	@Override
	public void unsubscribeChildrenChange(String path, ChildrenListener listener) {
		if (StringUtils.isBlank(path) || listener == null) {
			throw new IllegalArgumentException("没有指定路径或监听器为空！");
		}
		String p = format(path);
		listeners.remove(p, listener);
	}

	@Override
	public void registe(final String path, final String data) {
		if (StringUtils.isBlank(path)) {
			throw new IllegalArgumentException("没有指定路径!");
		}
		String p = format(path);
		if (!client.exists(p)) {
			client.createPersistent(p, true);
		}
		client.writeData(p, data);
	}

	@Override
	public void writeData(String path, Object data) {
		if (StringUtils.isBlank(path)) {
			throw new IllegalArgumentException("没有指定路径!");
		}
		String p = format(path);
		client.writeData(p, data);
	}

	@Override
	public Object readData(String path) {
		if (StringUtils.isBlank(path)) {
			throw new IllegalArgumentException("没有指定路径!");
		}
		String p = format(path);
		return client.readData(p);
	}

	@Override
	public void unregiste(final String path) {
		if (StringUtils.isBlank(path)) {
			throw new IllegalArgumentException("没有指定路径!");
		}
		String p = format(path);
		if (client.countChildren(p) == 0) {
			client.delete(p);
		}
		listeners.remove(p);
	}

	private String format(String path) {
		String p = path;
		if (path.endsWith("/")) {
			p = path.substring(0, path.length() - 2);
		}
		return p;
	}

	@Override
	public String createEphemeralSequential(String path, Object data) {
		return client.createEphemeralSequential(path, data);
	}

	@Override
	public boolean delete(String path) {
		return client.delete(path);
	}

	@Override
	public boolean exist(String path) {
		if (StringUtils.isBlank(path)) {
			throw new IllegalArgumentException("没有指定路径!");
		}
		String p = format(path);
		return client.exists(p);
	}

	@Override
	public void createPersistent(String path) {
		if (StringUtils.isBlank(path)) {
			throw new IllegalArgumentException("没有指定路径!");
		}
		String p = format(path);
		if (!exist(p)) {
			client.createPersistent(p, true);
		}
	}

}
