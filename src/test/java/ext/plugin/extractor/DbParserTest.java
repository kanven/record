package ext.plugin.extractor;

import org.junit.Test;

import com.kanven.record.ext.plugins.extract.db.parser.SchemaParseHandler;

public class DbParserTest {

	@Test
	public void testParse() {
		System.out.println(
				SchemaParseHandler.parse("uic*[uic_user(id,mobile_phone,real_name,update_time:true,update_emp)]"));
	}

}
