package com.cy.modules.planning.agent.liteflow;

import com.cy.modules.planning.agent.entity.MaterialAvailability;
import com.cy.modules.planning.agent.enums.MaterialStatus;
import com.cy.modules.planning.agent.mapper.MaterialAvailabilityMapper;
import com.cy.modules.planning.agent.service.ProcurementCoordinationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

/**
 * LiteFlow node component for procurement coordination check.
 * Delegates to ProcurementCoordinationService to generate Purchase Requests
 * for materials with shortages detected in the previous MaterialCheck step.
 */
@Slf4j
@Component("procurementCheck")
public class ProcurementCheckCmp extends NodeComponent {

    @Resource
    private ProcurementCoordinationService procurementCoordinationService;

    @Resource
    private MaterialAvailabilityMapper materialAvailabilityMapper;

    @Override
    public void process() throws Exception {
        log.info("[PlanningChain] ProcurementCheck node started");

        // Find all materials with shortage status that need PR generation
        LambdaQueryWrapper<MaterialAvailability> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialAvailability::getStatus, MaterialStatus.SHORTAGE.getValue());

        List<MaterialAvailability> shortages = materialAvailabilityMapper.selectList(wrapper);

        if (shortages.isEmpty()) {
            log.info("[PlanningChain] No material shortages requiring procurement");
            return;
        }

        int prCount = 0;
        for (MaterialAvailability shortage : shortages) {
            if (shortage.getDeficitQty() != null && shortage.getDeficitQty().signum() > 0) {
                // Use order deadline as a proxy for production start date
                LocalDate productionStartDate = LocalDate.now().plusDays(7);
                procurementCoordinationService.generatePurchaseRequest(
                        shortage.getOrderId(),
                        shortage.getMaterialId(),
                        shortage.getDeficitQty(),
                        productionStartDate
                );
                prCount++;
            }
        }

        log.info("[PlanningChain] ProcurementCheck node completed. Generated {} Purchase Requests", prCount);
    }
}
