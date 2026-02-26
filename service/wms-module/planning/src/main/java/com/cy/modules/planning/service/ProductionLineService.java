package com.cy.modules.planning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.modules.planning.entity.ProductionLine;

import java.util.List;

/**
 * @Description: Production Line Service
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface ProductionLineService extends IService<ProductionLine> {

    List<ProductionLine> getByStatus(String status);

    boolean isCodeUnique(String lineCode, String excludeId);
}
