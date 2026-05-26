package com.cy.modules.planning.agent.liteflow;

import com.cy.modules.planning.agent.entity.PlanningOrder;
import com.cy.modules.planning.agent.mapper.PlanningOrderMapper;
import com.cy.modules.planning.agent.service.FinishedGoodsDispatchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * LiteFlow node component for dispatch notification.
 * Delegates to FinishedGoodsDispatchService to notify the sales warehouse
 * for dispatch scheduling when orders are fully fulfilled.
 */
@Slf4j
@Component("dispatchNotification")
public class DispatchNotificationCmp extends NodeComponent {

    @Resource
    private FinishedGoodsDispatchService finishedGoodsDispatchService;

    @Resource
    private PlanningOrderMapper planningOrderMapper;

    @Override
    public void process() throws Exception {
        log.info("[ExecutionChain] DispatchNotification node started");

        // Find all fully fulfilled orders that need dispatch notification
        LambdaQueryWrapper<PlanningOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlanningOrder::getFulfillmentStatus, "fully_fulfilled");

        List<PlanningOrder> fulfilledOrders = planningOrderMapper.selectList(wrapper);

        int dispatchedCount = 0;
        for (PlanningOrder order : fulfilledOrders) {
            try {
                finishedGoodsDispatchService.notifyDispatch(order.getId());
                dispatchedCount++;
            } catch (Exception e) {
                log.error("[ExecutionChain] Failed to notify dispatch for order: {}", order.getId(), e);
            }
        }

        log.info("[ExecutionChain] DispatchNotification node completed. Dispatched {} orders", dispatchedCount);
    }
}
