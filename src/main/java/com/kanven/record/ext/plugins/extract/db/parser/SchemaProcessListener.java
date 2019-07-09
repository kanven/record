package com.kanven.record.ext.plugins.extract.db.parser;

import org.apache.commons.lang3.StringUtils;

import com.kanven.record.ext.plugins.extract.db.parser.Schema.SchemaBuilder;
import com.kanven.record.ext.plugins.extract.db.parser.SchemaParser.AllContext;
import com.kanven.record.ext.plugins.extract.db.parser.SchemaParser.FieldNameContext;
import com.kanven.record.ext.plugins.extract.db.parser.SchemaParser.FlagContext;
import com.kanven.record.ext.plugins.extract.db.parser.SchemaParser.NormalContext;
import com.kanven.record.ext.plugins.extract.db.parser.SchemaParser.PrefixContext;
import com.kanven.record.ext.plugins.extract.db.parser.SchemaParser.SuffixContext;
import com.kanven.record.ext.plugins.extract.db.parser.SchemaParser.TableContext;
import com.kanven.record.ext.plugins.extract.db.parser.SchemaParser.TableNameContext;
import com.kanven.record.ext.plugins.extract.db.parser.Table.Field.FieldBuilder;
import com.kanven.record.ext.plugins.extract.db.parser.Table.TableBuilder;

public class SchemaProcessListener extends SchemaBaseListener {

	private SchemaBuilder sb;

	private TableBuilder tb;

	private FieldBuilder fb;

	private boolean flag;

	public SchemaProcessListener(SchemaBuilder sb) {
		this.sb = sb;
	}

	@Override
	public void enterNormal(NormalContext ctx) {
		sb.db(ctx.getText());
	}

	@Override
	public void enterPrefix(PrefixContext ctx) {
		String text = ctx.getText();
		sb.prefix(text.substring(0, text.length() - 1));
	}

	@Override
	public void enterSuffix(SuffixContext ctx) {
		String text = ctx.getText();
		sb.suffix(text.substring(1));
	}

	@Override
	public void enterTable(TableContext ctx) {
		tb = TableBuilder.newInstance();
	}

	@Override
	public void exitTable(TableContext ctx) {
		Table table = tb.build();
		sb.table(table);
	}

	@Override
	public void enterTableName(TableNameContext ctx) {
		tb.name(ctx.getText());
	}

	@Override
	public void enterAll(AllContext ctx) {
		tb.all(true);
	}

	@Override
	public void enterFieldName(FieldNameContext ctx) {
		fb = FieldBuilder.newInstance();
		// 标志复位
		this.flag = false;
	}

	@Override
	public void exitFieldName(FieldNameContext ctx) {
		String field = ctx.getText();
		if (flag) {
			if (StringUtils.isNoneBlank(field)) {
				String[] items = field.split(":");
				fb.name(items[0]);
			}
		} else {
			fb.name(field);
		}
		tb.field(fb.build());
	}

	@Override
	public void enterFlag(FlagContext ctx) {
		this.flag = true;
		fb.ignored(Boolean.parseBoolean(ctx.getText()));
	}

}
