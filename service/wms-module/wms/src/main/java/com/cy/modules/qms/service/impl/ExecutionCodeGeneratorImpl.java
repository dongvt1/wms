package com.cy.modules.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cy.modules.qms.entity.InspectionExecution;
import com.cy.modules.qms.mapper.InspectionExecutionMapper;
import com.cy.modules.qms.service.ExecutionCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implementation sinh mã execution theo format EXCyyyyMMddNNN.
 * Thread-safe với synchronized để đảm bảo uniqueness trong môi trường concurrent.
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
@Service
public class ExecutionCodeGeneratorImpl implements ExecutionCodeGenerator {

    private static final String PREFIX = "EXC";

    @Autowired
    private InspectionExecutionMapper inspectionExecutionMapper;

    @Override
    public synchronized String generateCode() {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String prefix = PREFIX + dateStr;

        // Query the last execution code for today, ordered descending to get the max
        QueryWrapper<InspectionExecution> qw = new QueryWrapper<>();
        qw.likeRight("execution_code", prefix)
          .orderByDesc("execution_code")
          .last("LIMIT 1");

        InspectionExecution last = inspectionExecutionMapper.selectOne(qw);

        int seq = 1;
        if (last != null && last.getExecutionCode() != null) {
            try {
                String code = last.getExecutionCode();
                // Extract the last 3 digits (NNN part)
                String seqStr = code.substring(code.length() - 3);
                seq = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                seq = 1;
            }
        }

        return prefix + String.format("%03d", seq);
    }
}
