package ext.plugin.extractor;

import org.junit.Test;

import com.kanven.record.ext.plugins.extract.db.DbParser;
import com.kanven.record.ext.plugins.extract.db.Rule;

public class DbParserTest {

	private String content = "uic*.[uic_user(id,mobile_phone,user_card_no,wx_id,union_id,is_subscribe_wx,subscribe_wx_time,cancel_subscribe_wx_time,is_bind_wx,bind_wx_time,fwc_id,is_subscribe_fwc,cancel_bind_wx_time,subscribe_fwc_time,cancel_subscribe_fwc_time,is_bind_fwc,bind_fwc_time,cancel_bind_fwc_time,sf_pay_open_id,is_face_auth,is_real_name,status,is_del,create_time,update_time:true,update_emp:true)|uic_courier_role_detail(id,user_id,audit_status,effective_time,company_id,dept_code,staff_id,last_login_time,is_company_call,status,is_del,remark,update_time:true,update_emp:true)|uic_open_user_detail(id,user_id,open_id,type,is_subscribe,subscribe_time,cancel_subscribe_time,is_bind,bind_time,cancel_bind_time,create_time,create_emp,status,is_del,update_time:true,update_emp:true)]";

	private DbParser parser = new DbParser();

	@Test
	public void testParse() {
		Rule rule = parser.parse(content);
		System.out.println(rule);
	}

}
