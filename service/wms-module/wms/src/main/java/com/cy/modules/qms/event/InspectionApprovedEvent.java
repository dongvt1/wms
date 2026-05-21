package com.cy.modules.qms.event;

import org.springframework.context.ApplicationEvent;

/**
 * Spring Application Event phát ra khi một Inspection Execution được phê duyệt (approved).
 * Được sử dụng để trigger giải phóng QC block trên production stage
 * và cập nhật số lượng lỗi trên Work Order nếu kết quả FAIL.
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
public class InspectionApprovedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    /** ID phiên kiểm tra đã được approved */
    private final String executionId;

    /** Kết quả tổng thể: pass hoặc fail */
    private final String overallResult;

    /** ID lệnh sản xuất (Work Order) - có thể null */
    private final String workOrderId;

    public InspectionApprovedEvent(Object source,
                                    String executionId,
                                    String overallResult,
                                    String workOrderId) {
        super(source);
        this.executionId = executionId;
        this.overallResult = overallResult;
        this.workOrderId = workOrderId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getOverallResult() {
        return overallResult;
    }

    public String getWorkOrderId() {
        return workOrderId;
    }

    @Override
    public String toString() {
        return "InspectionApprovedEvent{" +
                "executionId='" + executionId + '\'' +
                ", overallResult='" + overallResult + '\'' +
                ", workOrderId='" + workOrderId + '\'' +
                '}';
    }
}
