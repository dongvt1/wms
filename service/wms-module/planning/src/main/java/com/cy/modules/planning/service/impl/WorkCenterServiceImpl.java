package com.cy.modules.planning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.modules.planning.entity.WorkCenter;
import com.cy.modules.planning.mapper.WorkCenterMapper;
import com.cy.modules.planning.service.WorkCenterService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: Work Center Service Implementation
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Service
public class WorkCenterServiceImpl extends ServiceImpl<WorkCenterMapper, WorkCenter>
        implements WorkCenterService {

    @Override
    public List<WorkCenter> getByStatus(String status) {
        return baseMapper.selectByStatus(status);
    }

    @Override
    public List<WorkCenter> getByProductionLineId(String productionLineId) {
        return baseMapper.selectByProductionLineId(productionLineId);
    }

    @Override
    public boolean isCodeUnique(String centerCode, String excludeId) {
        QueryWrapper<WorkCenter> qw = new QueryWrapper<>();
        qw.eq("center_code", centerCode);
        if (excludeId != null) {
            qw.ne("id", excludeId);
        }
        return count(qw) == 0;
    }
}
