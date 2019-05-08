package com.kanven.record.ext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.kanven.record.exception.RecordException;
import com.kanven.record.ext.plugins.alarm.AlarmPlugin;
import com.kanven.record.ext.plugins.extract.ExtractorPlugin;
import com.kanven.record.ext.plugins.load.LoadPlugin;
import com.kanven.record.ext.plugins.register.RegisterPlugin;
import com.kanven.record.ext.plugins.transform.TransformPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 * 
 * @author kanven
 *
 */
public final class PluginContext {

	private static final String BASE_PACKAGE = "com.fcbox.record.ext.plugins";

	private static final Map<String, Class<? extends ExtractorPlugin>> extractors = new HashMap<>();

	private static final Map<String, Class<? extends TransformPlugin>> transforms = new HashMap<>();

	private static final Map<String, Class<? extends LoadPlugin>> loads = new HashMap<>();

	private static final Map<String, Class<? extends RegisterPlugin>> registers = new HashMap<>();
	
	private static final Map<String, Class<? extends AlarmPlugin>> alarms = new HashMap<>();

	public final static Class<? extends ExtractorPlugin> getExtractor(String name) {
		return extractors.get(name);
	}

	public final static Class<? extends TransformPlugin> getTransform(String name) {
		return transforms.get(name);
	}

	public final static Class<? extends LoadPlugin> getLoad(String name) {
		return loads.get(name);
	}

	public final static Class<? extends RegisterPlugin> getRegister(String name) {
		return registers.get(name);
	}
	
	public final static Class<? extends AlarmPlugin> getAlarm(String name){
		return alarms.get(name);
	}

	static {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setUrls(ClasspathHelper.forPackage(BASE_PACKAGE)).setScanners(new SubTypesScanner(),
				new TypeAnnotationsScanner());
		Reflections reflections = new Reflections(cb);
		loadPlugins(reflections.getSubTypesOf(ExtractorPlugin.class), PluginContext.extractors);
		loadPlugins(reflections.getSubTypesOf(TransformPlugin.class), PluginContext.transforms);
		loadPlugins(reflections.getSubTypesOf(LoadPlugin.class), PluginContext.loads);
		loadPlugins(reflections.getSubTypesOf(RegisterPlugin.class), PluginContext.registers);
		loadPlugins(reflections.getSubTypesOf(AlarmPlugin.class), PluginContext.alarms);
	}

	private static <T> void loadPlugins(Set<Class<? extends T>> plugins, Map<String, Class<? extends T>> container) {
		for (Class<? extends T> plugin : plugins) {
			if (!plugin.isAnnotationPresent(Plugin.class)) {
				throw new RecordException(plugin.getName() + " class isn't have a FlowAnnotation");
			}
			Plugin annotation = plugin.getAnnotation(Plugin.class);
			String name = annotation.name();
			if (container.containsKey(name)) {
				throw new RecordException(plugin.getName() + " container already exist the " + name + " plugin");
			}
			container.put(name, plugin);
		}
	}

}
