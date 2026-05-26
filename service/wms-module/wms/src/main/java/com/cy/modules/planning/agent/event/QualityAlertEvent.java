package com.cy.modules.planning.agent.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * Sự kiện cảnh báo chất lượng khi tỷ lệ lỗi vượt ngưỡng.
 * Được publish khi QualityIntegrationService phát hiện defect rate vượt trung bình 30 ngày > 5 điểm phần trăm.
 */
@Getter
public class QualityAlertEvent extends ApplicationEvent {

    /** ID batch bị ảnh hưởng */
    private final String batchId;

    /** Loại sản phẩm */
    private final String productId;

    /** ID dây chuyền sản xuất */
    private final String lineId;

    /** Tỷ lệ lỗi hiện tại */
    private final BigDecimal currentDefectRate;

    /** Tỷ lệ lỗi trung bình 30 ngày */
    private final BigDecimal averageDefectRate;

    public QualityAlertEvent(Object source, String batchId, String productId, String lineId,
                             BigDecimal currentDefectRate, BigDecimal averageDefectRate) {
        super(source);
        this.batchId = batchId;
        this.productId = productId;
        this.lineId = lineId;
        this.currentDefectRate = currentDefectRate;
        this.averageDefectRate = averageDefectRate;
    }
}
