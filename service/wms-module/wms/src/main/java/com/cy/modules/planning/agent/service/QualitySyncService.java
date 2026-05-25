package com.cy.modules.planning.agent.service;

import java.math.BigDecimal;

/**
 * Service interface cho đồng bộ dữ liệu chất lượng từ QMS (Quality Management System).
 * Thực hiện polling định kỳ mỗi 15 phút để lấy dữ liệu chất lượng,
 * cache tỷ lệ lỗi và kết quả kiểm tra, tính toán trung bình cuốn 30/90 ngày.
 */
public interface QualitySyncService {

    /**
     * Thực hiện đồng bộ dữ liệu chất lượng từ QMS.
     * Lấy dữ liệu defect rate và inspection results,
     * tính toán rolling averages, cache vào Redis,
     * cập nhật trạng thái đồng bộ trong ap_sync_status.
     */
    void syncQualityData();

    /**
     * Kích hoạt đồng bộ thủ công (manual trigger).
     */
    void triggerManualSync();

    /**
     * Lấy tỷ lệ lỗi trung bình 30 ngày cho sản phẩm trên dây chuyền.
     *
     * @param productId mã sản phẩm
     * @param lineId    mã dây chuyền sản xuất
     * @return tỷ lệ lỗi trung bình 30 ngày (%), null nếu không có dữ liệu
     */
    BigDecimal getDefectRate30Day(String productId, String lineId);

    /**
     * Lấy tỷ lệ yield trung bình 90 ngày cho sản phẩm trên dây chuyền.
     *
     * @param productId mã sản phẩm
     * @param lineId    mã dây chuyền sản xuất
     * @return tỷ lệ yield 90 ngày (%), null nếu không có dữ liệu
     */
    BigDecimal getYieldRate90Day(String productId, String lineId);

    /**
     * Kiểm tra dữ liệu chất lượng có bị stale (>30 phút không cập nhật) hay không.
     *
     * @return true nếu dữ liệu đã cũ hơn 30 phút
     */
    boolean isDataStale();
}
