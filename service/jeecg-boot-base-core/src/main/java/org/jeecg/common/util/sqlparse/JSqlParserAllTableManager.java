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
//import org.jeecg.common.util.sqlparse.vo.SelectSqlInfo;
//
//import java.io.StringReader;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * Classes that resolve all table names and fields
// */
//@Slf4j
//public class JSqlParserAllTableManager {
//
//    private final String sql;
//    private final Map<String, SelectSqlInfo> allTableMap = new HashMap<>();
//    /**
//     * The alias corresponds to the actual table name
//     */
//    private final Map<String, String> tableAliasMap = new HashMap<>();
//
//    /**
//     * parsedsql
//     */
//    private String parsedSql = null;
//
//    JSqlParserAllTableManager(String selectSql) {
//        this.sql = selectSql;
//    }
//
//    /**
//     * Start parsing
//     *
//     * @return
//     * @throws JSQLParserException
//     */
//    public Map<String, SelectSqlInfo> parse() throws JSQLParserException {
//        // 1. Create parser
//        CCJSqlParserManager mgr = new CCJSqlParserManager();
//        // 2. parse using parsersqlGenerate hierarchicaljavakind
//        Statement stmt = mgr.parse(new StringReader(this.sql));
//        if (stmt instanceof Select) {
//            Select selectStatement = (Select) stmt;
//            SelectBody selectBody = selectStatement.getSelectBody();
//            this.parsedSql = selectBody.toString();
//            // 3. parseselectQuerysqlinformation
//            if (selectBody instanceof PlainSelect) {
//                PlainSelect plainSelect = (PlainSelect) selectBody;
//                // 4. merge fromItems
//                List<FromItem> fromItems = new ArrayList<>();
//                fromItems.add(plainSelect.getFromItem());
//                // 4.1 deal withjointable
//                List<Join> joins = plainSelect.getJoins();
//                if (joins != null) {
//                    joins.forEach(join -> fromItems.add(join.getRightItem()));
//                }
//                // 5. deal with fromItems
//                for (FromItem fromItem : fromItems) {
//                    // 5.1 By table namefrom
//                    if (fromItem instanceof Table) {
//                        this.addSqlInfoByTable((Table) fromItem);
//                    }
//                    // 5.2 通过子Queryof方式from
//                    else if (fromItem instanceof SubSelect) {
//                        this.handleSubSelect((SubSelect) fromItem);
//                    }
//                }
//                // 6. parse selectFields
//                List<SelectItem> selectItems = plainSelect.getSelectItems();
//                for (SelectItem selectItem : selectItems) {
//                    // 6.1 Queryof是全部字段
//                    if (selectItem instanceof AllColumns) {
//                        // when selectItem for AllColumns hour，fromItem 必定for Table
//                        String tableName = plainSelect.getFromItem(Table.class).getName();
//                        // 此处必定不for空，因for在parse fromItem hour，The table name has been added to allTableMap middle
//                        SelectSqlInfo sqlInfo = this.allTableMap.get(tableName);
//                        assert sqlInfo != null;
//                        // 设置forQuery全部字段
//                        sqlInfo.setSelectAll(true);
//                        sqlInfo.setSelectFields(null);
//                        sqlInfo.setRealSelectFields(null);
//                    }
//                    // 6.2 Queryof是带表别名（ u.* )All fields of
//                    else if (selectItem instanceof AllTableColumns) {
//                        AllTableColumns allTableColumns = (AllTableColumns) selectItem;
//                        String aliasName = allTableColumns.getTable().getName();
//                        // Get table name by alias
//                        String tableName = this.tableAliasMap.get(aliasName);
//                        if (tableName == null) {
//                            tableName = aliasName;
//                        }
//                        SelectSqlInfo sqlInfo = this.allTableMap.get(tableName);
//                        // 如果此处for空，则说明该字段是通过子Query获取of，所以可以不deal with，只有实际表才需要deal with
//                        if (sqlInfo != null) {
//                            // 设置forQuery全部字段
//                            sqlInfo.setSelectAll(true);
//                            sqlInfo.setSelectFields(null);
//                            sqlInfo.setRealSelectFields(null);
//                        }
//                    }
//                    // 6.3 各种字段表达式deal with
//                    else if (selectItem instanceof SelectExpressionItem) {
//                        SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
//                        Expression expression = selectExpressionItem.getExpression();
//                        Alias alias = selectExpressionItem.getAlias();
//                        this.handleExpression(expression, alias, plainSelect.getFromItem());
//                    }
//                }
//            } else {
//                log.warn("暂hour尚未deal with该kind型of SelectBody: {}", selectBody.getClass().getName());
//                throw new JeecgBootException("暂hour尚未deal with该kind型of SelectBody");
//            }
//        } else {
//            // No select Querysql，不做deal with
//            throw new JeecgBootException("No select Querysql，不做deal with");
//        }
//        return this.allTableMap;
//    }
//
//    /**
//     * deal with子Query
//     *
//     * @param subSelect
//     */
//    private void handleSubSelect(SubSelect subSelect) {
//        try {
//            String subSelectSql = subSelect.getSelectBody().toString();
//            // 递归调用parse
//            Map<String, SelectSqlInfo> map = JSqlParserUtils.parseAllSelectTable(subSelectSql);
//            if (map != null) {
//                this.assignMap(map);
//            }
//        } catch (Exception e) {
//            log.error("parse子Query出错", e);
//        }
//    }
//
//    /**
//     * deal withQuery字段表达式
//     *
//     * @param expression
//     */
//    private void handleExpression(Expression expression, Alias alias, FromItem fromItem) {
//        // deal with函数式字段  CONCAT(name,'(',age,')')
//        if (expression instanceof Function) {
//            Function functionExp = (Function) expression;
//            List<Expression> expressions = functionExp.getParameters().getExpressions();
//            for (Expression expItem : expressions) {
//                this.handleExpression(expItem, null, fromItem);
//            }
//            return;
//        }
//        // deal with字段上of子Query
//        if (expression instanceof SubSelect) {
//            this.handleSubSelect((SubSelect) expression);
//            return;
//        }
//        // 不deal with字面量
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
//        // deal with字段
//        if (expression instanceof Column) {
//            Column column = (Column) expression;
//            // Query字段名
//            String fieldName = column.getColumnName();
//            String aliasName = fieldName;
//            if (alias != null) {
//                aliasName = alias.getName();
//            }
//            String tableName;
//            if (column.getTable() != null) {
//                // 通过列table名获取 sqlInfo
//                // For example user.name，Here's tableName that is user
//                tableName = column.getTable().getName();
//                // Maybe an alias，需要转换for真实表名
//                if (this.tableAliasMap.get(tableName) != null) {
//                    tableName = this.tableAliasMap.get(tableName);
//                }
//            } else {
//                // whencolumnoftablefor空hour，The explanation is fromItem middleof字段
//                tableName = ((Table) fromItem).getName();
//            }
//            SelectSqlInfo $sqlInfo = this.allTableMap.get(tableName);
//            if ($sqlInfo != null) {
//                $sqlInfo.addSelectField(aliasName, fieldName);
//            } else {
//                log.warn("Something unexpected happened，未找到表名for {} of SelectSqlInfo", tableName);
//            }
//        }
//    }
//
//    /**
//     * Add based on table namesqlInfo
//     *
//     * @param table
//     */
//    private void addSqlInfoByTable(Table table) {
//        String tableName = table.getName();
//        // parse aliasName
//        if (table.getAlias() != null) {
//            this.tableAliasMap.put(table.getAlias().getName(), tableName);
//        }
//        SelectSqlInfo sqlInfo = new SelectSqlInfo(this.parsedSql);
//        sqlInfo.setFromTableName(table.getName());
//        this.allTableMap.put(sqlInfo.getFromTableName(), sqlInfo);
//    }
//
//    /**
//     * mergemap
//     *
//     * @param source
//     */
//    private void assignMap(Map<String, SelectSqlInfo> source) {
//        for (Map.Entry<String, SelectSqlInfo> entry : source.entrySet()) {
//            SelectSqlInfo sqlInfo = this.allTableMap.get(entry.getKey());
//            if (sqlInfo == null) {
//                this.allTableMap.put(entry.getKey(), entry.getValue());
//            } else {
//                // merge
//                if (sqlInfo.getSelectFields() == null) {
//                    sqlInfo.setSelectFields(entry.getValue().getSelectFields());
//                } else {
//                    sqlInfo.getSelectFields().addAll(entry.getValue().getSelectFields());
//                }
//                if (sqlInfo.getRealSelectFields() == null) {
//                    sqlInfo.setRealSelectFields(entry.getValue().getRealSelectFields());
//                } else {
//                    sqlInfo.getRealSelectFields().addAll(entry.getValue().getRealSelectFields());
//                }
//            }
//        }
//    }
//
//}
