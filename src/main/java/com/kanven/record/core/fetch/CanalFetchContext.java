package com.kanven.record.core.fetch;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.kanven.record.core.flow.FlowController;
import com.kanven.record.ext.plugins.extract.ExtractorPlugin;
import com.kanven.record.ext.plugins.load.LoadPlugin;
import com.kanven.record.ext.plugins.transform.TransformPlugin;

/**
 * 非线程安全类
 * 
 * @author kanven
 *
 */
public class CanalFetchContext {

	private static final ConcurrentMap<Long, FlowController> flows = new ConcurrentHashMap<>();

	private final static Map<Long, Map<String, ExtractorPlugin>> extractors = new HashMap<>();

	private final static Map<Long, Map<String, TransformPlugin>> transforms = new HashMap<>();

	private final static Map<Long, Map<String, LoadPlugin>> loads = new HashMap<>();

	public static void addLoad(Long piplineId, String load, LoadPlugin plugin) {
		addPlugin(piplineId, load, plugin, loads);
	}

	public static LoadPlugin getLoad(Long piplineId, String load) {
		Map<String, LoadPlugin> containers = loads.get(piplineId);
		if (containers == null) {
			return null;
		}
		return containers.get(load);
	}

	public static ExtractorPlugin getExtractor(Long piplineId, String extractor) {
		Map<String, ExtractorPlugin> containers = extractors.get(piplineId);
		if (containers == null) {
			return null;
		}
		return containers.get(extractor);
	}

	public static void addExtractor(Long piplineId, String extractor, ExtractorPlugin plugin) {
		addPlugin(piplineId, extractor, plugin, extractors);
	}

	public static TransformPlugin getTransform(Long piplineId, String transform) {
		Map<String, TransformPlugin> containers = transforms.get(piplineId);
		if (containers == null) {
			return null;
		}
		return containers.get(transform);
	}

	public static void addTransform(Long piplineId, String transform, TransformPlugin plugin) {
		addPlugin(piplineId, transform, plugin, transforms);
	}

	private static <T> void addPlugin(Long piplineId, String name, T plugin, Map<Long, Map<String, T>> plugins) {
		Map<String, T> ps = plugins.get(piplineId);
		if (ps == null) {
			ps = new HashMap<String, T>(1);
			plugins.put(piplineId, ps);
		}
		ps.put(name, plugin);
	}

	public static FlowController getFlow(Long piplineId, int parallelism) {
		if (piplineId == null) {
			throw new NullPointerException("pipline id should not be null or nagetive number");
		}
		FlowController controller = flows.get(piplineId);
		if (controller == null) {
			if (piplineId <= 0) {
				controller = flows.putIfAbsent(piplineId, new FlowController());
			} else {
				controller = flows.putIfAbsent(piplineId, new FlowController(parallelism));
			}
			if (controller == null) {
				controller = flows.get(piplineId);
			}
		}
		return controller;
	}

	public static FlowController getFlow(Long piplineId) {
		return getFlow(piplineId, -1);
	}

}
