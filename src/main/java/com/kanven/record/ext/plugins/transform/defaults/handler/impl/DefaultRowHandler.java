package com.kanven.record.ext.plugins.transform.defaults.handler.impl;

import com.kanven.record.core.meta.Column;
import com.kanven.record.core.meta.Row;
import com.kanven.record.exception.RecordException;
import com.kanven.record.ext.plugins.transform.defaults.handler.Handler;
import org.apache.commons.lang3.StringUtils;

public class DefaultRowHandler implements Handler {
	
	private String[] schema = new String[2];
	
	@Override
	public void handle(Row row) {
		String schema = row.schema();
		if(StringUtils.isBlank(schema)){
			throw new RecordException("the schema is null");
		}
		String regx = this.schema[0];
		if(StringUtils.isNoneBlank(regx)){
			if(schema.startsWith(regx)){
				String name = this.schema[1];
				if(StringUtils.isNoneBlank(name)){
					row.schema(name);
				}
			}
		}
	}
	
	public void schema(String schama){
		if(StringUtils.isNotBlank(schama)){
			if(schama.contains("->")){
				String[] items = schama.split("->");
				if(items.length != 2){
					throw new RecordException("the shema role for transform is error");
				}
				String regex = StringUtils.trim(items[0]);
				if(StringUtils.isBlank(regex)){
					throw new RecordException("the schema regex is null");
				}
				if(regex.contains("*")){
					String[] parts = regex.split("\\*");
					if(parts.length != 1){
						throw new RecordException("the shema role for transform is error");
					}
					regex = parts[0];
				}
				String name = StringUtils.trim(items[1]);
				if(StringUtils.isBlank(name)){
					throw new RecordException("the schema name is null");
				}
				this.schema[0] = regex;
				this.schema[1] = name;
			}
		}
	}
	
	public static void main(String[] args) {
		String rule = "-> uic";
		DefaultRowHandler handler = new DefaultRowHandler();
		handler.schema(rule);
		Column column = Column.ColumnBuilder.newBuilder().key(false).name("name").value("tt").index(1).type("varchar").build();
		Row.RowBuilder builder = Row.RowBuilder.newBuilder().schema("uic1").table("tt").column(column);
		handler.handle(builder.build());
	}

}
