package com.kanven.record.ext.plugins.alarm;

public interface AlarmPlugin {
	
	void send(String topic,String content);
	
	void close();
	
}
