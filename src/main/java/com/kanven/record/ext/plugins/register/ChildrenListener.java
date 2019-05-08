package com.kanven.record.ext.plugins.register;

import java.util.List;

public interface ChildrenListener {

	void onNotify(List<String> values);

	void onSession();
	
}
