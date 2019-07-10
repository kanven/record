package com.kanven.record.core;

import java.util.HashMap;
import java.util.Map;

import com.kanven.record.core.fetch.FetcherConfig;
import com.kanven.record.ext.plugins.alarm.Alarm;
import com.kanven.record.ext.plugins.register.Register;

public class Context {

	private final static Map<Long, FetcherConfig> configs = new HashMap<>();

	private static Register registerPlugin;

	private static Alarm alarmPlugin;

	public static void config(Long piplineId, FetcherConfig config) {
		configs.put(piplineId, config);
	}

	public static FetcherConfig config(Long piplineId) {
		return configs.get(piplineId);
	}

	public static Register register() {
		return registerPlugin;
	}

	public static void register(Register registerPlugin) {
		Context.registerPlugin = registerPlugin;
	}

	public static void alarm(Alarm alarmPlugin) {
		Context.alarmPlugin = alarmPlugin;
	}

	public static Alarm alarm() {
		return Context.alarmPlugin;
	}

}
