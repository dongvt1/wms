//package org.jeecg.common.util.sqlparse;
//
//import lombok.extern.slf4j.Slf4j;
//import net.sf.jsqlparser.JSQLParserException;
//import net.sf.jsqlparser.expression.*;
//import net.sf.jsqlparser.parser.CCJSqlParserManager;
//import net.sf.jsqlparser.schema.Column;
//import net.sf.jsqlparser.schema.Table;
//import net.sf.jsqlparser.statement.Statement;
//import net.sf.jsqlparser.statement.select.*;
//import org.jeecg.common.exception.JeecgBootException;
//import org.jeecg.common.util.oConvertUtils;
//import org.jeecg.common.util.sqlparse.vo.SelectSqlInfo;
//
//import java.io.StringReader;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//public class JSqlParserUtils {
//
//    /**
//     * parse Query（select）sqlinformation，
//     * 此方法会展开所有子Query到一个mapinside，
//     * keyOnly the real table names are saved，如果Query的没有真实的table name，will be ignored。
//     * valueOnly store real field names，如果Query的没有真实的字段名，will be ignored。
//     * <p>
//     * For example：SELECT a.*,d.age,(SELECT count(1) FROM sys_depart) AS count FROM (SELECT username AS foo, realname FROM sys_user) a, demo d
//     * parse后的结果为：{sys_user=[username, realname], demo=[age], sys_depart=[]}
//     *
//     * @param selectSql
//     * @return
//     */
//    public static Map<String, SelectSqlInfo> parseAllSelectTable(String selectSql) throws JSQLParserException {
//        if (oConvertUtils.isEmpty(selectSql)) {
//            return null;
//        }
//        // log.info("parseQuerySql：{}", selectSql);
//        JSqlParserAllTableManager allTableManager = new JSqlParserAllTableManager(selectSql);
//        return allTableManager.parse();
//    }
//
//    /**
//     * parse Query（select）sqlinformation，子Query嵌套
//     *
//     * @param selectSql
//     * @return
//     */
//    public static SelectSqlInfo parseSelectSqlInfo(String selectSql) throws JSQLParserException {
//        if (oConvertUtils.isEmpty(selectSql)) {
//            return null;
//        }
//        // log.info("parseQuerySql：{}", selectSql);
//        // use JSqlParer parsesql
//        // 1、创建parse器
//        CCJSqlParserManager mgr = new CCJSqlParserManager();
//        // 2、useparse器parsesqlGenerate hierarchicaljavakind
//        Statement stmt = mgr.parse(new StringReader(selectSql));
//        if (stmt instanceof Select) {
//            Select selectStatement = (Select) stmt;
//            // 3、parseselectQuerysqlinformation
//            return JSqlParserUtils.parseBySelectBody(selectStatement.getSelectBody());
//        } else {
//            // No select Querysql，No processing
//            throw new JeecgBootException("No select Querysql，No processing");
//        }
//    }
//
//    /**
//     * parse select Querysqlinformation
//     *
//     * @param selectBody
//     * @return
//     */
//    private static SelectSqlInfo parseBySelectBody(SelectBody selectBody) {
//        // 判断是否use了unionWait for operations
//        if (selectBody instanceof SetOperationList) {
//            // 如果use了unionWait for operations，则只parse第一个Query
//            List<SelectBody> selectBodyList = ((SetOperationList) selectBody).getSelects();
//            return JSqlParserUtils.parseBySelectBody(selectBodyList.get(0));
//        }
//        // simpleselectQuery
//        if (selectBody instanceof PlainSelect) {
//            SelectSqlInfo sqlInfo = new SelectSqlInfo(selectBody);
//            PlainSelect plainSelect = (PlainSelect) selectBody;
//            FromItem fromItem = plainSelect.getFromItem();
//            // parse aliasName
//            if (fromItem.getAlias() != null) {
//                sqlInfo.setFromTableAliasName(fromItem.getAlias().getName());
//            }
//            // parse table name
//            if (fromItem instanceof Table) {
//                // 通过table name的方式from
//                Table fromTable = (Table) fromItem;
//                sqlInfo.setFromTableName(fromTable.getName());
//            } else if (fromItem instanceof SubSelect) {
//                // 通过子Query的方式from
//                SubSelect fromSubSelect = (SubSelect) fromItem;
//                SelectSqlInfo subSqlInfo = JSqlParserUtils.parseBySelectBody(fromSubSelect.getSelectBody());
//                sqlInfo.setFromSubSelect(subSqlInfo);
//            }
//            // parse selectFields
//            List<SelectItem> selectItems = plainSelect.getSelectItems();
//            for (SelectItem selectItem : selectItems) {
//                if (selectItem instanceof AllColumns || selectItem instanceof AllTableColumns) {
//                    // All fields
//                    sqlInfo.setSelectAll(true);
//                    sqlInfo.setSelectFields(null);
//                    sqlInfo.setRealSelectFields(null);
//                    break;
//                } else if (selectItem instanceof SelectExpressionItem) {
//                    // 获取单个Query字段名
//                    SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
//                    Expression expression = selectExpressionItem.getExpression();
//                    Alias alias = selectExpressionItem.getAlias();
//                    JSqlParserUtils.handleExpression(sqlInfo, expression, alias);
//                }
//            }
//            return sqlInfo;
//        } else {
//            log.warn("暂时尚未处理该kind型的 SelectBody: {}", selectBody.getClass().getName());
//            throw new JeecgBootException("暂时尚未处理该kind型的 SelectBody");
//        }
//    }
//
//    /**
//     * 处理Query字段表达式
//     *
//     * @param sqlInfo
//     * @param expression
//     * @param alias      Is there an alias?，No biographynull
//     */
//    private static void handleExpression(SelectSqlInfo sqlInfo, Expression expression, Alias alias) {
//        // Handle functional fields  CONCAT(name,'(',age,')')
//        if (expression instanceof Function) {
//            JSqlParserUtils.handleFunctionExpression((Function) expression, sqlInfo);
//            return;
//        }
//        // 处理字段上的子Query
//        if (expression instanceof SubSelect) {
//            SubSelect subSelect = (SubSelect) expression;
//            SelectSqlInfo subSqlInfo = JSqlParserUtils.parseBySelectBody(subSelect.getSelectBody());
//            // Note：字段上的子Query，必须只Query一个字段，Otherwise an error will be reported，So you can merge with confidence
//            sqlInfo.getSelectFields().addAll(subSqlInfo.getSelectFields());
//            sqlInfo.getRealSelectFields().addAll(subSqlInfo.getAllRealSelectFields());
//            return;
//        }
//        // Do not handle literals
//        if (expression instanceof StringValue ||
//                expression instanceof NullValue ||
//                expression instanceof LongValue ||
//                expression instanceof DoubleValue ||
//                expression instanceof HexValue ||
//                expression instanceof DateValue ||
//                expression instanceof TimestampValue ||
//                expression instanceof TimeValue
//        ) {
//            return;
//        }
//
//        // Query字段名
//        String selectField = expression.toString();
//        // 实际Query字段名
//        String realSelectField = selectField;
//        // 判断Is there an alias?
//        if (alias != null) {
//            selectField = alias.getName();
//        }
//        // Get the real field name
//        if (expression instanceof Column) {
//            Column column = (Column) expression;
//            realSelectField = column.getColumnName();
//        }
//        sqlInfo.addSelectField(selectField, realSelectField);
//    }
//
//    /**
//     * Handle functional fields
//     *
//     * @param functionExp
//     * @param sqlInfo
//     */
//    private static void handleFunctionExpression(Function functionExp, SelectSqlInfo sqlInfo) {
//        List<Expression> expressions = functionExp.getParameters().getExpressions();
//        for (Expression expression : expressions) {
//            JSqlParserUtils.handleExpression(sqlInfo, expression, null);
//        }
//    }
//
//}
