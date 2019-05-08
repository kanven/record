package com.kanven.record.ext.plugins.extract.db;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.Map;

import com.kanven.record.exception.RecordException;

/**
 * 
 * @author kanven
 *
 */
public class DbParser {

	public Rule parse(String rule) {
		Rule r = new Rule();
		int index = rule.indexOf(".");
		String db = rule.substring(0, index);
		parseDb(r, db);
		String tables = rule.substring(index + 1);
		parseTable(r, tables);
		return r;
	}

	private void parseDb(Rule rule, String db) {
		if (db.contains("*")) {
			String[] items = db.split("\\*");
			if (items.length == 1) {
				rule.setDbPrefix(items[0]);
			} else if (items.length == 2) {
				rule.setDbPrefix(items[0]);
				rule.setDbSuffix(items[1]);
			} else {
				throw new RecordException("the parser unsupport the rule:" + db);
			}
		} else {
			rule.setDb(db);
		}
	}

	private void parseTable(Rule rule, String format) {
		CharacterIterator iterator = new StringCharacterIterator(format);
		int start = iterator.getBeginIndex();
		int end = iterator.getEndIndex() - 1;
		StringBuilder tsb = null;
		StringBuilder csb = null;
		boolean bt = false;
		boolean bc = false;
		String table = null;
		Map<String, Rule.ColumnRule> columns = null;
		for (int i = start; i <= end;) {
			char c = iterator.current();
			switch (c) {
			case '[':
				if (i == start) {
					bt = true;
					tsb = new StringBuilder();
					break;
				}
				throw new RecordException("tables rule format error,only the start char must be '['");
			case '(':
				char pre = iterator.previous();
				if (pre == '[' || pre == ',' || pre == '，' || pre == ']' || pre == ')' || pre == '(' || pre == '|'
						|| pre == '*') {
					throw new RecordException("tables rule format error");
				}
				iterator.setIndex(i);
				char next = iterator.next();
				if (next == '[' || next == ',' || next == '，' || next == ']' || next == ')' || next == '('
						|| next == '|') {
					throw new RecordException("tables rule format error");
				}
				bt = false;
				bc = true;
				csb = new StringBuilder();
				table = tsb.toString();
				columns = new HashMap<>();
				break;
			case ')':
				next = iterator.next();
				if (next != ']' && next != '|') {
					throw new RecordException("tables rule format error");
				}
				rule.addMapping(table, columns);
				columns = null;
				table = null;
				bt = true;
				bc = false;
				break;
			case '|':
				pre = iterator.previous();
				if (pre != ')') {
					throw new RecordException("tables rule format error");
				}
				iterator.setIndex(i);
				next = iterator.next();
				if (next == '[' || next == ',' || next == '，' || next == ']' || next == ')' || next == '('
						|| next == '|' || next == '*') {
					throw new RecordException("tables rule format error");
				}
				tsb = new StringBuilder();
				bt = true;
				bc = false;
				break;
			case ',':
			case '，':
				if (csb.length() > 0) {
					String text = csb.toString();
					String column = text;
					Rule.ColumnRule cr = new Rule.ColumnRule();
					if (text.contains(":")) {
						String[] items = text.split(":");
						column = items[0];
						if (items.length == 2) {
							boolean ignore = Boolean.parseBoolean(items[1]);
							cr.setIgnore(ignore);
						}
					}
					columns.put(column, cr);
					csb = new StringBuilder();
				}
				bt = false;
				bc = true;
				break;
			case '*':
				pre = iterator.previous();
				if (pre != '(') {
					throw new RecordException("tables rule format error");
				}
				iterator.setIndex(i);
				next = iterator.next();
				if (next != ')') {
					throw new RecordException("tables rule format error");
				}
				rule.addMapping(table, columns);
				bt = false;
				bc = true;
				break;
			case ']':
				if (i == end) {
					bt = false;
					bc = false;
					break;
				}
				throw new RecordException("tables rule format error,only the end char must be ']'");
			default:
				if (bt) {
					tsb.append(c);
				} else if (bc) {
					csb.append(c);
				}
				break;
			}
			iterator.setIndex(++i);
		}
	}

}
