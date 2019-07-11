package ext.plugin.alarm;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.kanven.record.ext.plugins.alarm.sms.SmsAlarm;

public class SmsAlarmPluginTest {
	
	private SmsAlarm alarm;

	@Before
	public void before(){
		alarm = new SmsAlarm();
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
