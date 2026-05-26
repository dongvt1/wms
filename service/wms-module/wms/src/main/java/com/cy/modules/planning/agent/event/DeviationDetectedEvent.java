package com.cy.modules.planning.agent.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * Sự kiện phát hiện sai lệch sản xuất vượt ngưỡng 10%.
 * Được publish khi ProductionExecutionMonitor phát hiện deviation trong tiến độ sản xuất.
 */
@Getter
public class DeviationDetectedEvent extends ApplicationEvent {

    /** ID kế hoạch tuần bị ảnh hưởng */
    private final String weeklyPlanId;

    /** ID batch bị sai lệch */
    private final String batchId;

    /** Phần trăm sai lệch */
    private final BigDecimal deviationPercentage;

    public DeviationDetectedEvent(Object source, String weeklyPlanId, String batchId, BigDecimal deviationPercentage) {
        super(source);
        this.weeklyPlanId = weeklyPlanId;
        this.batchId = batchId;
        this.deviationPercentage = deviationPercentage;
    }
}
