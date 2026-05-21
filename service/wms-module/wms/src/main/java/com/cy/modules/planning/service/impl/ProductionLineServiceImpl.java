package com.cy.modules.planning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.modules.common.entity.ProductionLine;
import com.cy.modules.common.mapper.ProductionLineMapper;
import com.cy.modules.planning.service.ProductionLineService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: Production Line Service Implementation
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Service
public class ProductionLineServiceImpl extends ServiceImpl<ProductionLineMapper, ProductionLine>
        implements ProductionLineService {

    @Override
    public List<ProductionLine> getByStatus(String status) {
        return baseMapper.selectByStatus(status);
    }

    @Override
    public boolean isCodeUnique(String lineCode, String excludeId) {
        QueryWrapper<ProductionLine> qw = new QueryWrapper<>();
        qw.eq("line_code", lineCode);
        if (excludeId != null) {
            qw.ne("id", excludeId);
        }
        return count(qw) == 0;
    }
}
