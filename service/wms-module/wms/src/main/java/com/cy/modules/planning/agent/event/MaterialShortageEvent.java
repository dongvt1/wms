package com.cy.modules.planning.agent.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Sự kiện thiếu nguyên vật liệu được phát hiện khi kiểm tra tồn kho.
 * Được publish khi MaterialAvailabilityService phát hiện deficit cho đơn hàng.
 */
@Getter
public class MaterialShortageEvent extends ApplicationEvent {

    /** ID đơn hàng bị ảnh hưởng */
    private final String orderId;

    /** Danh sách nguyên vật liệu thiếu: materialId -> deficit quantity */
    private final Map<String, BigDecimal> materialDeficits;

    public MaterialShortageEvent(Object source, String orderId, Map<String, BigDecimal> materialDeficits) {
        super(source);
        this.orderId = orderId;
        this.materialDeficits = materialDeficits;
    }
}
