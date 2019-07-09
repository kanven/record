package com.kanven.record.ext.plugins.register;

public interface Register {

	void registe(String path, String data);

	void unregiste(String path);

	void subscribeChildrenChange(String path, ChildrenListener listener);

	void unsubscribeChildrenChange(String path, ChildrenListener listener);

	void subscribeDataChange(String path, DataListener listener);

	void unsubscribeDataChange(String path, DataListener listener);

	String createEphemeralSequential(String path, Object data);

	void createPersistent(String path);

	boolean exist(String path);

	boolean delete(String path);

	void writeData(String path, Object data);

	Object readData(String path);

	void close() throws Exception;

}
