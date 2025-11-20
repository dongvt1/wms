package org.jeecg.modules.airag.demo;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.modules.airag.flow.component.enhance.IAiRagEnhanceJava;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JavaEnhanceDemo: Exceldata reader
 * for [QQYUN-11718]【AI】Building block report dockingAIProcess orchestration interface display report
 * @Author: chenrui
 * @Date: 2025/4/29 16:51
 */
@Component("jimuDataReader")
@Slf4j
public class JimuDataReader implements IAiRagEnhanceJava {


    @Override
    public Map<String, Object> process(Map<String, Object> inputParams) {
        // inputParams: {"bizData":"/xxxx/xxxx/xxxx/xxxx.xls"}
        try {
            String filePath = (String) inputParams.get("bizData");
            if (filePath == null || filePath.isEmpty()) {
                throw new IllegalArgumentException("File path is empty");
            }

            File excelFile = new File(filePath);
            if (!excelFile.exists() || !excelFile.isFile()) {
                throw new IllegalArgumentException("File not found: " + filePath);
            }

            // Since we don't know the target entity class, we'll read the Excel generically
            return readExcelData(excelFile);
        } catch (Exception e) {
            log.error("Error processing Excel file", e);
            throw new JeecgBootBizTipException("calljavaEnhance失败", e);
        }
    }

    /**
     * ExcelImport tool methods，based onExcelImportUtil
     *
     * @param file Exceldocument
     * @return ExcelRead results，Contains fields and data
     * @throws Exception Exception during import
     */
    public static Map<String, Object> readExcelData(File file) throws Exception {
        Map<String, Object> result = new HashMap<>();

        // Set import parameters
        ImportParams params = new ImportParams();
        params.setTitleRows(0); // no title
        params.setHeadRows(1);  // The first line is the header

        // readExceldata
        List<Map<String, Object>> dataList = ExcelImportUtil.importExcel(file, Map.class, params);

        // 如果没有data，Return empty result
        if (dataList == null || dataList.isEmpty()) {
            result.put("fields", new ArrayList<>());
            result.put("datas", new ArrayList<>());
            return result;
        }

        // 从第一行data中获取字段名
        List<String> fieldNames = new ArrayList<>(dataList.get(0).keySet());

        result.put("fields", fieldNames);
        result.put("datas", dataList);

        return result;
    }

}
