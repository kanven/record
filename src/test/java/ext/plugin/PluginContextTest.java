package ext.plugin;

import org.junit.Assert;
import org.junit.Test;

import com.kanven.record.ext.PluginContext;
import com.kanven.record.ext.plugins.alarm.Alarm;

public class PluginContextTest {

	@Test
	public void testLoadAlarm() throws InstantiationException, IllegalAccessException {
		Class<? extends Alarm> clazz = PluginContext.getAlarm("sms");
		Alarm alarm = clazz.newInstance();
		Assert.assertNotNull(alarm);
	}

}
