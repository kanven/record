// Generated from schema.g4 by ANTLR 4.7.2

package com.kanven.record.ext.plugins.extract.db.parser;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SchemaParser}.
 */
public interface SchemaListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SchemaParser#schema}.
	 * @param ctx the parse tree
	 */
	void enterSchema(SchemaParser.SchemaContext ctx);
	/**
	 * Exit a parse tree produced by {@link SchemaParser#schema}.
	 * @param ctx the parse tree
	 */
	void exitSchema(SchemaParser.SchemaContext ctx);
	/**
	 * Enter a parse tree produced by the {@code normal}
	 * labeled alternative in {@link SchemaParser#db}.
	 * @param ctx the parse tree
	 */
	void enterNormal(SchemaParser.NormalContext ctx);
	/**
	 * Exit a parse tree produced by the {@code normal}
	 * labeled alternative in {@link SchemaParser#db}.
	 * @param ctx the parse tree
	 */
	void exitNormal(SchemaParser.NormalContext ctx);
	/**
	 * Enter a parse tree produced by the {@code prefix}
	 * labeled alternative in {@link SchemaParser#db}.
	 * @param ctx the parse tree
	 */
	void enterPrefix(SchemaParser.PrefixContext ctx);
	/**
	 * Exit a parse tree produced by the {@code prefix}
	 * labeled alternative in {@link SchemaParser#db}.
	 * @param ctx the parse tree
	 */
	void exitPrefix(SchemaParser.PrefixContext ctx);
	/**
	 * Enter a parse tree produced by the {@code suffix}
	 * labeled alternative in {@link SchemaParser#db}.
	 * @param ctx the parse tree
	 */
	void enterSuffix(SchemaParser.SuffixContext ctx);
	/**
	 * Exit a parse tree produced by the {@code suffix}
	 * labeled alternative in {@link SchemaParser#db}.
	 * @param ctx the parse tree
	 */
	void exitSuffix(SchemaParser.SuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link SchemaParser#table}.
	 * @param ctx the parse tree
	 */
	void enterTable(SchemaParser.TableContext ctx);
	/**
	 * Exit a parse tree produced by {@link SchemaParser#table}.
	 * @param ctx the parse tree
	 */
	void exitTable(SchemaParser.TableContext ctx);
	/**
	 * Enter a parse tree produced by {@link SchemaParser#tableName}.
	 * @param ctx the parse tree
	 */
	void enterTableName(SchemaParser.TableNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SchemaParser#tableName}.
	 * @param ctx the parse tree
	 */
	void exitTableName(SchemaParser.TableNameContext ctx);
	/**
	 * Enter a parse tree produced by the {@code field}
	 * labeled alternative in {@link SchemaParser#tableC}.
	 * @param ctx the parse tree
	 */
	void enterField(SchemaParser.FieldContext ctx);
	/**
	 * Exit a parse tree produced by the {@code field}
	 * labeled alternative in {@link SchemaParser#tableC}.
	 * @param ctx the parse tree
	 */
	void exitField(SchemaParser.FieldContext ctx);
	/**
	 * Enter a parse tree produced by the {@code all}
	 * labeled alternative in {@link SchemaParser#tableC}.
	 * @param ctx the parse tree
	 */
	void enterAll(SchemaParser.AllContext ctx);
	/**
	 * Exit a parse tree produced by the {@code all}
	 * labeled alternative in {@link SchemaParser#tableC}.
	 * @param ctx the parse tree
	 */
	void exitAll(SchemaParser.AllContext ctx);
	/**
	 * Enter a parse tree produced by {@link SchemaParser#fieldName}.
	 * @param ctx the parse tree
	 */
	void enterFieldName(SchemaParser.FieldNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SchemaParser#fieldName}.
	 * @param ctx the parse tree
	 */
	void exitFieldName(SchemaParser.FieldNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SchemaParser#flag}.
	 * @param ctx the parse tree
	 */
	void enterFlag(SchemaParser.FlagContext ctx);
	/**
	 * Exit a parse tree produced by {@link SchemaParser#flag}.
	 * @param ctx the parse tree
	 */
	void exitFlag(SchemaParser.FlagContext ctx);
}