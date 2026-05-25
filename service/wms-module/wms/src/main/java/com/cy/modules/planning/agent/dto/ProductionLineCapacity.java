package com.cy.modules.planning.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO năng lực sản xuất của dây chuyền
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionLineCapacity {

    /** Mã dây chuyền sản xuất */
    private String lineId;

    /** Tên dây chuyền */
    private String lineName;

    /** Ngày bắt đầu */
    private LocalDate fromDate;

    /** Ngày kết thúc */
    private LocalDate toDate;

    /** Tổng giờ sản xuất khả dụng */
    private BigDecimal totalAvailableHours;

    /** Giờ đã được lên kế hoạch */
    private BigDecimal scheduledHours;

    /** Tỷ lệ sử dụng hiện tại (%) */
    private BigDecimal currentUtilization;

    /** Danh sách sản phẩm có thể sản xuất */
    private List<String> capableProducts;

    /** Chi tiết năng lực theo ngày */
    private List<DailyCapacity> dailyCapacities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyCapacity {
        /** Ngày */
        private LocalDate date;

        /** Giờ khả dụng */
        private BigDecimal availableHours;

        /** Giờ đã lên kế hoạch */
        private BigDecimal scheduledHours;

        /** Trạng thái bảo trì */
        private boolean maintenanceScheduled;
    }
}
