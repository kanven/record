package com.kanven.record.core;

import java.util.HashMap;
import java.util.Map;

import com.kanven.record.core.fetch.FetcherConfig;
import com.kanven.record.ext.plugins.alarm.AlarmPlugin;
import com.kanven.record.ext.plugins.register.RegisterPlugin;

public class Context {

	private final static Map<Long, FetcherConfig> configs = new HashMap<>();

	private static RegisterPlugin registerPlugin;

	private static AlarmPlugin alarmPlugin;

	public static void config(Long piplineId, FetcherConfig config) {
		configs.put(piplineId, config);
	}

	public static FetcherConfig config(Long piplineId) {
		return configs.get(piplineId);
	}

	public static RegisterPlugin register() {
		return registerPlugin;
	}

	public static void register(RegisterPlugin registerPlugin) {
		Context.registerPlugin = registerPlugin;
	}

	public static void alarm(AlarmPlugin alarmPlugin) {
		Context.alarmPlugin = alarmPlugin;
	}

	public static AlarmPlugin alarm() {
		return Context.alarmPlugin;
	}

}
