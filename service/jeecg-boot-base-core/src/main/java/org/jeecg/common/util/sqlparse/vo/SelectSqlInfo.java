//package org.jeecg.common.util.sqlparse.vo;
//
//import lombok.Data;
//import net.sf.jsqlparser.statement.select.SelectBody;
//
//import java.util.HashSet;
//import java.util.Set;
//
///**
// * select Query sql information
// */
//@Data
//public class SelectSqlInfo {
//
//    /**
//     * Query的表名，in the case of子Query，Then here isnull
//     */
//    private String fromTableName;
//    /**
//     * table alias
//     */
//    private String fromTableAliasName;
//    /**
//     * 通过子Query获取的表信息，For example：select name from (select * from user) u
//     * 如果不是子Query，Then it isnull
//     */
//    private SelectSqlInfo fromSubSelect;
//    /**
//     * Query的字段集合，in the case of * Then it isnull，如果设了别名Then it is别名
//     */
//    private Set<String> selectFields;
//    /**
//     * 真实的Query字段集合，in the case of * Then it isnull，如果设了别名Then it is原始字段名
//     */
//    private Set<String> realSelectFields;
//    /**
//     * 是否是Query所有字段
//     */
//    private boolean selectAll;
//
//    /**
//     * After parsing SQL （Keywords are all capitalized）
//     */
//    private final String parsedSql;
//
//    public SelectSqlInfo(String parsedSql) {
//        this.parsedSql = parsedSql;
//    }
//
//    public SelectSqlInfo(SelectBody selectBody) {
//        this.parsedSql = selectBody.toString();
//    }
//
//    public void addSelectField(String selectField, String realSelectField) {
//        if (this.selectFields == null) {
//            this.selectFields = new HashSet<>();
//        }
//        if (this.realSelectFields == null) {
//            this.realSelectFields = new HashSet<>();
//        }
//        this.selectFields.add(selectField);
//        this.realSelectFields.add(realSelectField);
//    }
//
//    /**
//     * Get all fields，包括子Query里的。
//     *
//     * @return
//     */
//    public Set<String> getAllRealSelectFields() {
//        Set<String> fields = new HashSet<>();
//        // 递归Get all fields，Give an intuitive method the name：
//        this.recursiveGetAllFields(this, fields);
//        return fields;
//    }
//
//    /**
//     * 递归Get all fields
//     */
//    private void recursiveGetAllFields(SelectSqlInfo sqlInfo, Set<String> fields) {
//        if (!sqlInfo.isSelectAll() && sqlInfo.getRealSelectFields() != null) {
//            fields.addAll(sqlInfo.getRealSelectFields());
//        }
//        if (sqlInfo.getFromSubSelect() != null) {
//            recursiveGetAllFields(sqlInfo.getFromSubSelect(), fields);
//        }
//    }
//
//    @Override
//    public String toString() {
//        return "SelectSqlInfo{" +
//                "fromTableName='" + fromTableName + '\'' +
//                ", fromSubSelect=" + fromSubSelect +
//                ", aliasName='" + fromTableAliasName + '\'' +
//                ", selectFields=" + selectFields +
//                ", realSelectFields=" + realSelectFields +
//                ", selectAll=" + selectAll +
//                "}";
//    }
//
//}
