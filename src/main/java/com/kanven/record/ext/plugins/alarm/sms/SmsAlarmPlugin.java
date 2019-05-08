package com.kanven.record.ext.plugins.alarm.sms;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.kanven.record.exception.RecordException;
import com.kanven.record.ext.plugins.alarm.AlarmPlugin;
import org.apache.commons.lang3.StringUtils;

import com.kanven.record.ext.Plugin;
import com.kanven.record.ext.PluginConfigUtil;

@Plugin(name = "sms")
public class SmsAlarmPlugin implements AlarmPlugin {

	private static final String DEFAULT_CONFIG_PATH = "ext/alarm/sms/sms.properties";


	private List<String> alarms = new ArrayList<>();

	public SmsAlarmPlugin() {
		this(DEFAULT_CONFIG_PATH);
	}

	public SmsAlarmPlugin(String configPath) {

	}

	@Override
	public void send(String topic, String content) {

	}

	@Override
	public void close() {

	}

}
