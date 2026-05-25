package com.cy.modules.planning.agent.controller;

import com.cy.modules.planning.agent.dto.SyncStatusDto;
import com.cy.modules.planning.agent.service.InventorySyncService;
import com.cy.modules.planning.agent.service.MachineSyncService;
import com.cy.modules.planning.agent.service.OrderSyncService;
import com.cy.modules.planning.agent.service.QualitySyncService;
import com.cy.modules.planning.agent.service.StalenessManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: Controller quản lý trạng thái đồng bộ hệ thống
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/planning-agent/sync")
@Tag(name = "Planning Agent - Sync Status", description = "API quản lý trạng thái đồng bộ hệ thống")
public class SyncStatusController {

    private static final List<String> VALID_SYSTEMS = Arrays.asList("orderhub", "erp", "scada", "qms");

    @Resource
    private StalenessManagementService stalenessManagementService;

    @Resource
    private OrderSyncService orderSyncService;

    @Resource
    private InventorySyncService inventorySyncService;

    @Resource
    private MachineSyncService machineSyncService;

    @Resource
    private QualitySyncService qualitySyncService;

    /**
     * Lấy trạng thái đồng bộ của tất cả hệ thống.
     * Bao gồm thông tin staleness, trạng thái chặn lập kế hoạch.
     */
    @GetMapping("/status")
    @Operation(summary = "Lấy trạng thái đồng bộ tất cả hệ thống",
            description = "Trả về trạng thái đồng bộ cho orderhub, erp, scada, qms với thông tin staleness")
    public Result<Map<String, SyncStatusDto>> getStatus() {
        Map<String, SyncStatusDto> statuses = stalenessManagementService.getAllSyncStatuses();
        boolean planningBlocked = stalenessManagementService.isPlanningBlocked();

        if (planningBlocked) {
            log.warn("[SyncStatus] Lập kế hoạch đang bị chặn do dữ liệu quá cũ");
            return Result.OK("Cảnh báo: Lập kế hoạch bị chặn do dữ liệu quá cũ (>60 phút)", statuses);
        }
        return Result.OK(statuses);
    }

    /**
     * Kích hoạt đồng bộ thủ công cho một hệ thống cụ thể.
     *
     * @param system tên hệ thống (orderhub, erp, scada, qms)
     */
    @PostMapping("/{system}/force-sync")
    @Operation(summary = "Kích hoạt đồng bộ thủ công",
            description = "Kích hoạt đồng bộ thủ công cho hệ thống được chỉ định")
    public Result<String> forceSync(@PathVariable("system") String system) {
        String systemLower = system.toLowerCase();

        if (!VALID_SYSTEMS.contains(systemLower)) {
            return Result.error("Hệ thống không hợp lệ: " + system
                    + ". Các hệ thống hỗ trợ: orderhub, erp, scada, qms");
        }

        log.info("[SyncStatus] Nhận yêu cầu force-sync cho hệ thống: {}", systemLower);

        try {
            switch (systemLower) {
                case "orderhub":
                    orderSyncService.triggerManualSync();
                    break;
                case "erp":
                    inventorySyncService.syncInventoryData();
                    break;
                case "scada":
                    machineSyncService.syncMachineStatuses();
                    break;
                case "qms":
                    qualitySyncService.triggerManualSync();
                    break;
                default:
                    return Result.error("Hệ thống không hợp lệ: " + system);
            }
            return Result.OK("Đồng bộ thủ công đã được kích hoạt cho hệ thống: " + systemLower);
        } catch (Exception e) {
            log.error("[SyncStatus] Lỗi khi kích hoạt force-sync cho {}: {}", systemLower, e.getMessage(), e);
            return Result.error("Lỗi khi kích hoạt đồng bộ: " + e.getMessage());
        }
    }
}
