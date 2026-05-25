package com.cy.modules.planning.agent.service;

import com.cy.modules.planning.agent.dto.SyncStatusDto;

import java.util.Map;

/**
 * Service quản lý độ cũ dữ liệu (staleness) của các hệ thống tích hợp.
 * <p>
 * Chức năng chính:
 * - Kiểm tra xem lập kế hoạch có bị chặn do dữ liệu quá cũ (>60 phút)
 * - Trả về trạng thái đồng bộ hiện tại của tất cả hệ thống
 * - Kích hoạt đối soát dữ liệu đầy đủ khi đồng bộ được khôi phục
 * - Cập nhật định kỳ giá trị staleness
 */
public interface StalenessManagementService {

    /**
     * Kiểm tra xem lập kế hoạch có bị chặn hay không.
     * Trả về true nếu bất kỳ hệ thống nào có dữ liệu cache vượt quá 60 phút staleness.
     *
     * @return true nếu lập kế hoạch bị chặn, false nếu cho phép
     */
    boolean isPlanningBlocked();

    /**
     * Lấy trạng thái đồng bộ hiện tại của tất cả hệ thống.
     * Key là tên hệ thống (orderhub, erp, scada, qms).
     *
     * @return map chứa trạng thái đồng bộ của từng hệ thống
     */
    Map<String, SyncStatusDto> getAllSyncStatuses();

    /**
     * Kích hoạt đối soát dữ liệu đầy đủ sau khi đồng bộ được khôi phục.
     * Phải hoàn thành trong vòng 10 phút kể từ khi đồng bộ được khôi phục.
     *
     * @param systemName tên hệ thống cần đối soát (orderhub, erp, scada, qms)
     */
    void triggerReconciliation(String systemName);

    /**
     * Tác vụ định kỳ cập nhật giá trị staleness cho tất cả hệ thống.
     * Được gọi tự động qua @Scheduled.
     */
    void checkAndUpdateStaleness();
}
