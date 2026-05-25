package com.cy.modules.planning.agent.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

/**
 * Sự kiện máy hỏng (breakdown) được phát hiện qua thay đổi trạng thái từ Scada.
 * Được publish khi trạng thái máy chuyển sang "breakdown" hoặc "error".
 */
@Getter
public class MachineBreakdownEvent extends ApplicationEvent {

    /** Mã dây chuyền sản xuất */
    private final String lineId;

    /** Mã máy bị hỏng */
    private final String machineId;

    /** Thời điểm phát hiện sự cố */
    private final Instant timestamp;

    public MachineBreakdownEvent(Object source, String lineId, String machineId, Instant timestamp) {
        super(source);
        this.lineId = lineId;
        this.machineId = machineId;
        this.timestamp = timestamp;
    }
}
