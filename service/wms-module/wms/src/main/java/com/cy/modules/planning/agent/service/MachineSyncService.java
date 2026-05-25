package com.cy.modules.planning.agent.service;

import com.cy.modules.planning.agent.dto.MachineStatus;

import java.util.List;

/**
 * Service đồng bộ trạng thái máy từ hệ thống Scada.
 * Poll mỗi 5 phút, cache dữ liệu vào Redis, phát hiện sự cố máy.
 */
public interface MachineSyncService {

    /**
     * Thực hiện đồng bộ trạng thái máy từ Scada.
     * Được gọi tự động mỗi 5 phút qua @Scheduled.
     */
    void syncMachineStatuses();

    /**
     * Lấy trạng thái máy đã cache cho một dây chuyền sản xuất.
     *
     * @param lineId mã dây chuyền sản xuất
     * @return danh sách trạng thái máy từ cache, hoặc empty list nếu chưa có
     */
    List<MachineStatus> getCachedMachineStatuses(String lineId);
}
