package ext.plugin.extractor;

import java.io.IOException;

import com.kanven.record.ext.plugins.extract.db.parser.SchemaParseHandler;

public class Antlr4Test {

	public static void main(String[] args) throws IOException {
		System.out.println(
				SchemaParseHandler.parse("uic*[uic_user(id,mobile_phone,real_name,update_time:true,update_emp)]"));
	}

}
