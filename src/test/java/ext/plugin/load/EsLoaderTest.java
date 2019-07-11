package ext.plugin.load;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.kanven.record.core.flow.FlowData;
import com.kanven.record.core.meta.Column;
import com.kanven.record.core.meta.Row;
import com.kanven.record.ext.plugins.load.Load;
import com.kanven.record.ext.plugins.load.es.EsLoader;

public class EsLoaderTest {

	@Test
	public void testLoadYear() {
		String config = "ext/load/es/test/year.properties";
		load(config);
	}

	@Test
	public void testNormal() {
		String config = "ext/load/es/test/normal.properties";
		load(config);
	}

	@Test
	public void testMonth() {
		String config = "ext/load/es/test/month.properties";
		load(config);
	}

	@Test
	public void testDay() {
		String config = "ext/load/es/test/day.properties";
		load(config);
	}

	private void load(String config) {
		Load load = new EsLoader(config);
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
		load.close();
	}

}
