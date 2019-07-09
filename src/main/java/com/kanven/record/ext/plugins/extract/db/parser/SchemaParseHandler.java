package com.kanven.record.ext.plugins.extract.db.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.kanven.record.exception.RecordException;
import com.kanven.record.ext.plugins.extract.db.parser.Schema.SchemaBuilder;

@SuppressWarnings("deprecation")
public class SchemaParseHandler {

	public static Schema parse(String rule) {
		Reader reader = null;
		try {
			reader = new StringReader(rule);
			ANTLRInputStream in = new ANTLRInputStream(reader);
			SchemaLexer lexer = new SchemaLexer(in);
			CommonTokenStream stream = new CommonTokenStream(lexer);
			SchemaParser parser = new SchemaParser(stream);
			ParseTree tree = parser.schema();
			ParseTreeWalker walker = new ParseTreeWalker();
			SchemaBuilder builder = SchemaBuilder.newInstance();
			walker.walk(new SchemaProcessListener(builder), tree);
			return builder.build();
		} catch (IOException e) {
			throw new RecordException(rule + " 解析出现异常", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
