package com.cy.modules.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cy.modules.qms.entity.InspectionTemplate;
import com.cy.modules.qms.mapper.InspectionTemplateMapper;
import com.cy.modules.qms.service.TemplateCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implementation sinh mã template theo format TPLyyyyMMddNNN.
 * Thread-safe với synchronized để đảm bảo uniqueness trong môi trường concurrent.
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
@Service
public class TemplateCodeGeneratorImpl implements TemplateCodeGenerator {

    private static final String PREFIX = "TPL";

    @Autowired
    private InspectionTemplateMapper inspectionTemplateMapper;

    @Override
    public synchronized String generateCode() {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String prefix = PREFIX + dateStr;

        // Query the last template code for today, ordered descending to get the max
        QueryWrapper<InspectionTemplate> qw = new QueryWrapper<>();
        qw.likeRight("template_code", prefix)
          .orderByDesc("template_code")
          .last("LIMIT 1");

        InspectionTemplate last = inspectionTemplateMapper.selectOne(qw);

        int seq = 1;
        if (last != null && last.getTemplateCode() != null) {
            try {
                String code = last.getTemplateCode();
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
