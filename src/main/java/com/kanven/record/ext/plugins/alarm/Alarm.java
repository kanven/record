package com.kanven.record.ext.plugins.alarm;

public interface Alarm {
	
	void send(String topic,String content);
	
	void close();
	
}
