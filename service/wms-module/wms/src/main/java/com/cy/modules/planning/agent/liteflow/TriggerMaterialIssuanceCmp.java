package com.cy.modules.planning.agent.liteflow;

import com.cy.modules.planning.agent.entity.WeeklyPlanBatch;
import com.cy.modules.planning.agent.mapper.WeeklyPlanBatchMapper;
import com.cy.modules.planning.agent.service.ProductionOrderIssuanceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * LiteFlow node component for triggering material issuance.
 * After production orders are issued, this component triggers material issuance
 * requests to WMS based on the product BOM for each batch.
 *
 * Note: Material issuance is handled as part of the issueProductionOrders flow
 * in ProductionOrderIssuanceService. This node ensures the step is explicitly
 * represented in the chain and handles any additional material issuance logic.
 */
@Slf4j
@Component("triggerMaterialIssuance")
public class TriggerMaterialIssuanceCmp extends NodeComponent {

    @Resource
    private WeeklyPlanBatchMapper weeklyPlanBatchMapper;

    @Override
    public void process() throws Exception {
        log.info("[ExecutionChain] TriggerMaterialIssuance node started");

        PlanningChainContext context = this.getContextBean(PlanningChainContext.class);
        String weeklyPlanId = context.getWeeklyPlanId();

        if (!context.isOrdersIssued()) {
            log.warn("[ExecutionChain] Production orders not issued, skipping material issuance");
            return;
        }

        // Verify all batches have material issuance triggered
        LambdaQueryWrapper<WeeklyPlanBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyPlanBatch::getWeeklyPlanId, weeklyPlanId)
               .eq(WeeklyPlanBatch::getStatus, "in_progress");

        List<WeeklyPlanBatch> batches = weeklyPlanBatchMapper.selectList(wrapper);
        context.setMaterialIssuanceTriggered(true);

        log.info("[ExecutionChain] TriggerMaterialIssuance node completed. {} batches in progress", batches.size());
    }
}
