package com.cy.modules.qms.service.impl;

import com.cy.modules.qms.entity.IqcInspection;
import com.cy.modules.qms.mapper.IqcInspectionMapper;
import com.cy.modules.qms.service.StockBlockingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @Description: Stock Blocking Service Implementation
 *               Cập nhật wh_stock_transaction.qc_status dựa trên kết quả IQC
 *               Sử dụng JdbcTemplate vì wh_stock_transaction thuộc module kho khác
 * @Author: BMad
 * @Date: 2026-03-01
 */
@Service
@Slf4j
public class StockBlockingServiceImpl implements StockBlockingService {

    @Autowired
    private IqcInspectionMapper iqcInspectionMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Mapping IQC result → qc_status:
     *   failed       → blocked
     *   conditional  → conditional_hold
     *   passed       → available
     */
    private String mapIqcStatusToQcStatus(String iqcStatus) {
        if (iqcStatus == null) return null;
        switch (iqcStatus.toLowerCase()) {
            case "failed":
                return "blocked";
            case "conditional":
                return "conditional_hold";
            case "passed":
                return "available";
            default:
                return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleIqcApproval(String inspectionId, String status) {
        if (inspectionId == null || inspectionId.trim().isEmpty()) {
            return "ID phiếu IQC không hợp lệ";
        }

        // Get the IQC inspection to find the linked stockTransactionId
        IqcInspection inspection = iqcInspectionMapper.selectById(inspectionId);
        if (inspection == null) {
            return "Không tìm thấy phiếu IQC";
        }

        String stockTransactionId = inspection.getStockTransactionId();
        if (stockTransactionId == null || stockTransactionId.trim().isEmpty()) {
            log.warn("Phiếu IQC {} không liên kết với phiếu nhập kho nào", inspectionId);
            return "Phiếu IQC không liên kết với phiếu nhập kho";
        }

        // Map IQC result to qc_status
        String qcStatus = mapIqcStatusToQcStatus(status);
        if (qcStatus == null) {
            return "Trạng thái IQC không hợp lệ: " + status;
        }

        // Update wh_stock_transaction.qc_status
        int rowsUpdated = jdbcTemplate.update(
                "UPDATE wh_stock_transaction SET qc_status = ? WHERE id = ?",
                qcStatus, stockTransactionId
        );

        if (rowsUpdated == 0) {
            log.warn("Không tìm thấy phiếu nhập kho {} để cập nhật qc_status", stockTransactionId);
            return "Không tìm thấy phiếu nhập kho liên kết";
        }

        log.info("Cập nhật qc_status của phiếu nhập kho {} thành '{}' (IQC: {}, kết quả: {})",
                stockTransactionId, qcStatus, inspectionId, status);

        return "Cập nhật trạng thái QC kho thành công: " + qcStatus;
    }

    @Override
    public boolean isStockAvailable(String stockTransactionId) {
        if (stockTransactionId == null || stockTransactionId.trim().isEmpty()) {
            return false;
        }

        List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT qc_status FROM wh_stock_transaction WHERE id = ?",
                stockTransactionId
        );

        if (results.isEmpty()) {
            log.warn("Không tìm thấy phiếu nhập kho: {}", stockTransactionId);
            return false;
        }

        Object qcStatusObj = results.get(0).get("qc_status");
        String qcStatus = qcStatusObj != null ? qcStatusObj.toString() : "pending";

        // Available if qc_status is 'available' or 'pending' (no QC check yet)
        return "available".equalsIgnoreCase(qcStatus) || "pending".equalsIgnoreCase(qcStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String releaseBlock(String stockTransactionId, String ncrId) {
        if (stockTransactionId == null || stockTransactionId.trim().isEmpty()) {
            return "ID phiếu nhập kho không hợp lệ";
        }
        if (ncrId == null || ncrId.trim().isEmpty()) {
            return "ID NCR không hợp lệ";
        }

        // Update qc_status to 'available' after NCR resolution
        int rowsUpdated = jdbcTemplate.update(
                "UPDATE wh_stock_transaction SET qc_status = 'available' WHERE id = ?",
                stockTransactionId
        );

        if (rowsUpdated == 0) {
            log.warn("Không tìm thấy phiếu nhập kho {} để giải phóng chặn", stockTransactionId);
            return "Không tìm thấy phiếu nhập kho";
        }

        log.info("Giải phóng chặn kho cho phiếu nhập kho {} sau khi NCR {} được xử lý",
                stockTransactionId, ncrId);

        return "Giải phóng chặn kho thành công";
    }
}
