package com.cy.modules.planning.agent.liteflow;

import com.cy.modules.planning.agent.entity.WeeklyPlanBatch;
import com.cy.modules.planning.agent.mapper.WeeklyPlanBatchMapper;
import com.cy.modules.planning.agent.service.ProductionExecutionMonitor;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * LiteFlow node component for recording finished goods.
 * Delegates to ProductionExecutionMonitor to record finished goods quantities
 * and trigger warehouse receipt in ERP for completed batches.
 */
@Slf4j
@Component("recordFinishedGoods")
public class RecordFinishedGoodsCmp extends NodeComponent {

    @Resource
    private ProductionExecutionMonitor productionExecutionMonitor;

    @Resource
    private WeeklyPlanBatchMapper weeklyPlanBatchMapper;

    @Override
    public void process() throws Exception {
        log.info("[ExecutionChain] RecordFinishedGoods node started");

        PlanningChainContext context = this.getContextBean(PlanningChainContext.class);
        String weeklyPlanId = context.getWeeklyPlanId();

        if (weeklyPlanId == null) {
            log.warn("[ExecutionChain] No weekly plan ID for recording finished goods");
            return;
        }

        // Find completed batches that need finished goods recording
        LambdaQueryWrapper<WeeklyPlanBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyPlanBatch::getWeeklyPlanId, weeklyPlanId)
               .eq(WeeklyPlanBatch::getStatus, "completed")
               .isNotNull(WeeklyPlanBatch::getActualQuantity);

        List<WeeklyPlanBatch> completedBatches = weeklyPlanBatchMapper.selectList(wrapper);

        int recordedCount = 0;
        for (WeeklyPlanBatch batch : completedBatches) {
            if (batch.getActualQuantity() != null && batch.getActualQuantity().signum() > 0) {
                productionExecutionMonitor.recordFinishedGoods(batch.getId(), batch.getActualQuantity());
                productionExecutionMonitor.generateMaterialReturn(batch.getId());
                recordedCount++;
            }
        }

        log.info("[ExecutionChain] RecordFinishedGoods node completed. Recorded {} batches", recordedCount);
    }
}
