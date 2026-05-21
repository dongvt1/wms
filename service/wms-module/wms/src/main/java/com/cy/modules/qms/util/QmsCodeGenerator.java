package com.cy.modules.qms.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Shared utility for generating sequential QMS codes.
 * <p>
 * Format: PREFIXyyyyMMddNNN
 * <ul>
 *   <li>PREFIX — type identifier (IQC, PQC, FQC, NCR, SK, RV)</li>
 *   <li>yyyyMMdd — current date</li>
 *   <li>NNN — zero-padded sequential number (001, 002, ...)</li>
 * </ul>
 *
 * @Author: BMad
 * @Date: 2026-02-25
 */
public class QmsCodeGenerator {

    private QmsCodeGenerator() {
        // utility class — no instantiation
    }

    /**
     * Generate the next sequential code for a given prefix.
     *
     * @param prefix     the code prefix (e.g. "IQC", "PQC", "FQC", "NCR", "SK", "RV")
     * @param codeColumn the database column name that stores the code (e.g. "inspection_code", "session_code")
     * @param mapper     any MyBatis-Plus BaseMapper whose entity contains the code column
     * @param <T>        entity type
     * @return the next code in format PREFIXyyyyMMddNNN
     */
    public static <T> String generateCode(String prefix, String codeColumn, BaseMapper<T> mapper) {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return generateCodeForDate(prefix, codeColumn, mapper, dateStr);
    }

    /**
     * Generate the next sequential code for a given prefix and date string.
     * <p>
     * This overload is useful for testing where the date needs to be controlled.
     *
     * @param prefix     the code prefix (e.g. "IQC", "PQC", "FQC", "NCR", "SK", "RV")
     * @param codeColumn the database column name that stores the code
     * @param mapper     any MyBatis-Plus BaseMapper whose entity contains the code column
     * @param dateStr    the date string in yyyyMMdd format
     * @param <T>        entity type
     * @return the next code in format PREFIXyyyyMMddNNN
     */
    public static <T> String generateCodeForDate(String prefix, String codeColumn, BaseMapper<T> mapper, String dateStr) {
        String prefixWithDate = prefix + dateStr;

        QueryWrapper<T> qw = new QueryWrapper<>();
        qw.select(codeColumn)
          .likeRight(codeColumn, prefixWithDate)
          .orderByDesc(codeColumn)
          .last("LIMIT 1");

        List<Object> results = mapper.selectObjs(qw);
        int seq = 1;
        if (results != null && !results.isEmpty() && results.get(0) != null) {
            try {
                String code = results.get(0).toString();
                seq = Integer.parseInt(code.substring(code.length() - 3)) + 1;
            } catch (NumberFormatException e) {
                seq = 1;
            }
        }
        return prefixWithDate + String.format("%03d", seq);
    }
}
