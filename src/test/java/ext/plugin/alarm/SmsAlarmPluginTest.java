package ext.plugin.alarm;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.kanven.record.ext.plugins.alarm.sms.SmsAlarmPlugin;

public class SmsAlarmPluginTest {
	
	private SmsAlarmPlugin alarm;

	@Before
	public void before(){
		alarm = new SmsAlarmPlugin();
	}
	
	
	@Test
	public void testSend(){
		alarm.send("测试", "哈哈哈");
	}
	
	
	@After
	public void after(){
		alarm.close();
	}
	
	
}
