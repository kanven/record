package ext.plugin.load;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.kanven.record.core.flow.FlowData;
import com.kanven.record.core.meta.Column;
import com.kanven.record.core.meta.Row;
import com.kanven.record.ext.plugins.load.Load;
import com.kanven.record.ext.plugins.load.es.EsLoader;

public class EsLoaderTest {

	private Load load;

	@Before
	public void before() {
		String config = "ext/load/es/test/es.properties";
		load = new EsLoader(config);
	}

	@Test
	public void testLoad() {
		FlowData fd = new FlowData(1L);
		List<List<Row>> rows = new ArrayList<>();
		List<Row> rs = new ArrayList<>();
		Set<Column> pr = new HashSet<>();
		Column id = new Column(1, "id", "string", "1", true);
		pr.add(id);
		Set<Column> cs = new HashSet<>();
		cs.add(id);
		Column name = new Column(2, "name", "string", "lili", true);
		cs.add(name);
		Row r = new Row("s", "user", pr, null, cs, System.currentTimeMillis());
		rs.add(r);
		rows.add(rs);
		fd.setRows(rows);
		load.load(fd);
	}

	@After
	public void after() {
		load.close();
	}

}
