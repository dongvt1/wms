package org.jeecg.modules.system.security;

import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.common.util.security.AbstractQueryBlackListHandler;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * dictionary component implementsqlPre-verification Check table dictionary only
 * dictCodeStringThe format is like：
 * table,text,code
 * table where xxx,text,code
 * table,text,code, where xxx
 *
 * @Author taoYan
 * @Date 2022/3/23 21:10
 **/
@Component("dictQueryBlackListHandler")
public class DictQueryBlackListHandler extends AbstractQueryBlackListHandler {

    @Override
    protected List<QueryTable> getQueryTableInfo(String dictCodeString) {
        //Decode escape characters
        try {
            if (dictCodeString.contains("%")) {
                dictCodeString = URLDecoder.decode(dictCodeString, "UTF-8");
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        dictCodeString = dictCodeString.trim();
        
        // no matter what scene second、The three elements must be fields of the table，directadd
        if (dictCodeString != null && dictCodeString.indexOf(SymbolConstant.COMMA) > 0) {
            String[] arr = dictCodeString.split(SymbolConstant.COMMA);
            if (arr.length != 3 && arr.length != 4) {
                return null;
            }
            
            //Get table name
            String tableName = getTableName(arr[0]);
            QueryTable table = new QueryTable(tableName, "");
            // no matter what scene second、The three elements must be fields of the table，directadd
            //Parameter field1
            table.addField(arr[1].trim());
            //Parameter field2
            String filed = arr[2].trim();
            if (oConvertUtils.isNotEmpty(filed)) {
                table.addField(filed);
            }
            List<QueryTable> list = new ArrayList<>();
            list.add(table);
            return list;
        }
        return null;
    }

    /**
     * PickwhereThe previous one is：table name
     *
     * @param str
     * @return
     */
    private String getTableName(String str) {
        String[] arr = str.split("\\s+(?i)where\\s+");
        String tableName = arr[0].trim();
        //【20230814】Solve using parameterstableName=sys_user t&Retest，Vulnerabilities still exist
        if (tableName.contains(".")) {
            tableName = tableName.substring(tableName.indexOf(".")+1, tableName.length()).trim();
        }
        if (tableName.contains(" ")) {
            tableName = tableName.substring(0, tableName.indexOf(" ")).trim();
        }
        
        //【issues/4393】 sys_user , (sys_user), sys_user%20, %60sys_user%60
        String reg = "\\s+|\\(|\\)|`";
        return tableName.replaceAll(reg, "");
    }

}
