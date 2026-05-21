package com.cy.modules.qms.event;

import org.springframework.context.ApplicationEvent;

/**
 * Spring Application Event phát ra khi một Routing Step hoàn thành.
 * Được sử dụng để trigger tự động tạo Inspection Execution
 * nếu routing step có liên kết QC stage.
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
public class RoutingStepCompletedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    /** ID routing step đã hoàn thành */
    private final String stepId;

    /** ID sản phẩm đang sản xuất */
    private final String productId;

    /** Loại QC stage (iqc, pqc, fqc) - null nếu routing step không có QC */
    private final String qcStageType;

    /** ID lệnh sản xuất (Work Order) */
    private final String workOrderId;

    /** ID công đoạn sản xuất (Production Stage) */
    private final String productionStageId;

    public RoutingStepCompletedEvent(Object source,
                                      String stepId,
                                      String productId,
                                      String qcStageType,
                                      String workOrderId,
                                      String productionStageId) {
        super(source);
        this.stepId = stepId;
        this.productId = productId;
        this.qcStageType = qcStageType;
        this.workOrderId = workOrderId;
        this.productionStageId = productionStageId;
    }

    public String getStepId() {
        return stepId;
    }

    public String getProductId() {
        return productId;
    }

    public String getQcStageType() {
        return qcStageType;
    }

    public String getWorkOrderId() {
        return workOrderId;
    }

    public String getProductionStageId() {
        return productionStageId;
    }

    @Override
    public String toString() {
        return "RoutingStepCompletedEvent{" +
                "stepId='" + stepId + '\'' +
                ", productId='" + productId + '\'' +
                ", qcStageType='" + qcStageType + '\'' +
                ", workOrderId='" + workOrderId + '\'' +
                ", productionStageId='" + productionStageId + '\'' +
                '}';
    }
}
