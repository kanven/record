package ext.plugin.extractor;

import org.junit.Assert;
import org.junit.Test;

import com.kanven.record.ext.plugins.extract.db.parser.Schema;
import com.kanven.record.ext.plugins.extract.db.parser.SchemaParseHandler;

public class DbParserTest {

	@Test
	public void testParse() {
		Schema schema = SchemaParseHandler
				.parse("uic*[uic_user(id,mobile_phone,real_name,update_time:true,update_emp)]");
		Assert.assertEquals(1, schema.tableSize());
	}

	@Test
	public void testMultTable() {
		Schema schema = SchemaParseHandler
				.parse("uic*[uic_user(id,mobile_phone,real_name,update_time:true,update_emp)uic_courier(id,user_id)]");
		Assert.assertEquals(2, schema.tableSize());
	}

	@Test
	public void testComment() {
		Schema schema = SchemaParseHandler.parse(
				"uic*[uic_user(id,mobile_phone,real_name,update_time:true,update_emp)/*uic_courier(id,user_id)*/]");
		Assert.assertEquals(1, schema.tableSize());
	}

}
