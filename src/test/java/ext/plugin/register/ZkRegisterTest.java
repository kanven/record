package ext.plugin.register;

import org.junit.Test;

import com.kanven.record.ext.plugins.register.Register;
import com.kanven.record.ext.plugins.register.zk.ZkRegister;

public class ZkRegisterTest {
	
	
	@Test
	public void testRegister(){
		Register register = new ZkRegister();
		register.createPersistent("/tt/tt");
		register.writeData("/tt/tt","{\"name\":\"kanven\",\"provence\":\"广东省深圳市\",\"age\":18}");
	}
	
}
