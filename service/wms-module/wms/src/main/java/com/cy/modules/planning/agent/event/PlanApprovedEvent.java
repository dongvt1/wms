package com.cy.modules.planning.agent.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Sự kiện kế hoạch được phê duyệt.
 * Được publish khi quản lý sản xuất phê duyệt kế hoạch tuần hoặc kế hoạch tháng.
 */
@Getter
public class PlanApprovedEvent extends ApplicationEvent {

    /** Loại kế hoạch: "weekly" hoặc "monthly" */
    private final String planType;

    /** ID kế hoạch được phê duyệt */
    private final String planId;

    /** Người phê duyệt */
    private final String approvedBy;

    public PlanApprovedEvent(Object source, String planType, String planId, String approvedBy) {
        super(source);
        this.planType = planType;
        this.planId = planId;
        this.approvedBy = approvedBy;
    }
}
