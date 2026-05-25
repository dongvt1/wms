package com.cy.modules.planning.agent.client;

import com.cy.modules.planning.agent.dto.DefectClassification;
import com.cy.modules.planning.agent.dto.QualityReport;

import java.time.LocalDate;

/**
 * Client interface cho tích hợp với hệ thống QMS (Quality Management System).
 * Cung cấp khả năng lấy dữ liệu chất lượng và phân loại sản phẩm lỗi.
 */
public interface QmsClient {

    /**
     * Lấy báo cáo chất lượng cho sản phẩm trên dây chuyền trong khoảng thời gian.
     *
     * @param productId mã sản phẩm
     * @param lineId    mã dây chuyền sản xuất
     * @param from      ngày bắt đầu
     * @param to        ngày kết thúc
     * @return báo cáo chất lượng
     */
    QualityReport getQualityData(String productId, String lineId, LocalDate from, LocalDate to);

    /**
     * Phân loại sản phẩm lỗi theo batch (sửa chữa được / phải hủy).
     *
     * @param batchId mã batch cần phân loại
     * @return kết quả phân loại lỗi
     */
    DefectClassification classifyDefects(String batchId);
}
