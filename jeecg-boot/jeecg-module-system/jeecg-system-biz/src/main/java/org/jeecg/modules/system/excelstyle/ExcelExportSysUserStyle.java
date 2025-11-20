package org.jeecg.modules.system.excelstyle;

import org.apache.poi.ss.usermodel.*;
import org.jeecgframework.poi.excel.export.styler.ExcelExportStylerDefaultImpl;

/**
 * @Description: Import users to get the title header style Override default style
 *
 * @author: wangshuai
 * @date: 2025/8/28 14:05
 */
public class ExcelExportSysUserStyle extends ExcelExportStylerDefaultImpl {

    public ExcelExportSysUserStyle(Workbook workbook) {
        super(workbook);
    }

    /**
     * Get title style
     *
     * @param color
     * @return
     */
    public CellStyle getHeaderStyle(short color) {
        CellStyle titleStyle = this.workbook.createCellStyle();
        Font font = this.workbook.createFont();
        font.setFontHeightInPoints((short)12);
        titleStyle.setFont(font);
        titleStyle.setAlignment(HorizontalAlignment.LEFT);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setWrapText(true);
        return titleStyle;
    }
}
