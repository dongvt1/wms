package com.cy.modules.qms.event;

import com.cy.modules.common.entity.ProductionStage;
import com.cy.modules.common.mapper.ProductionStageMapper;
import com.cy.modules.planning.entity.WorkOrder;
import com.cy.modules.planning.mapper.WorkOrderMapper;
import com.cy.modules.qms.dto.InspectionExecutionDTO;
import com.cy.modules.qms.service.InspectionExecutionService;
import com.cy.modules.qms.vo.InspectionExecutionVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.math.BigDecimal;

/**
 * Spring Event Listener xử lý sự kiện WMS integration cho QMS.
 *
 * Lắng nghe 2 loại event:
 * 1. RoutingStepCompletedEvent: Khi routing step hoàn thành → tạo InspectionExecution + block production stage
 * 2. InspectionApprovedEvent: Khi inspection được approved → release block + cập nhật defect count
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
@Component
public class QmsRoutingStepEventListener {

    private static final Logger log = LoggerFactory.getLogger(QmsRoutingStepEventListener.class);

    @Autowired
    private InspectionExecutionService inspectionExecutionService;

    @Autowired
    private ProductionStageMapper productionStageMapper;

    @Autowired(required = false)
    private WorkOrderMapper workOrderMapper;

    /**
     * Xử lý sự kiện routing step hoàn thành.
     * Nếu routing step có qc_stage_type → tạo InspectionExecution và block production stage.
     *
     * @param event RoutingStepCompletedEvent chứa thông tin routing step
     */
    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void handleRoutingStepCompleted(RoutingStepCompletedEvent event) {
        String qcStageType = event.getQcStageType();

        // Chỉ xử lý nếu routing step có liên kết QC stage
        if (qcStageType == null || qcStageType.trim().isEmpty()) {
            log.debug("Routing step {} không có QC stage, bỏ qua.", event.getStepId());
            return;
        }

        log.info("Routing step {} hoàn thành với QC stage type '{}'. " +
                        "Bắt đầu tạo Inspection Execution cho product {} trên work order {}.",
                event.getStepId(), qcStageType, event.getProductId(), event.getWorkOrderId());

        try {
            // 1. Tạo InspectionExecution mới
            InspectionExecutionDTO dto = new InspectionExecutionDTO();
            dto.setProductId(event.getProductId());
            dto.setStageType(qcStageType);
            dto.setWorkOrderId(event.getWorkOrderId());
            dto.setProductionStageId(event.getProductionStageId());
            dto.setNotes("Tự động tạo từ routing step: " + event.getStepId());

            InspectionExecutionVO executionVO = inspectionExecutionService.createExecution(dto);

            log.info("Đã tạo Inspection Execution '{}' (ID: {}) cho routing step {}.",
                    executionVO.getExecutionCode(), executionVO.getId(), event.getStepId());

            // 2. Set qc_blocked = 1 và liên kết execution ID trên production stage
            String productionStageId = event.getProductionStageId();
            if (productionStageId != null && !productionStageId.trim().isEmpty()) {
                ProductionStage productionStage = productionStageMapper.selectById(productionStageId);
                if (productionStage != null) {
                    productionStage.setQcBlocked(1);
                    productionStage.setQcExecutionId(executionVO.getId());
                    productionStageMapper.updateById(productionStage);

                    log.info("Đã set qc_blocked=1 trên production stage '{}'. " +
                                    "Chặn chuyển bước tiếp theo cho đến khi inspection được approved.",
                            productionStageId);
                } else {
                    log.warn("Không tìm thấy production stage với ID: {}. " +
                            "Không thể set qc_blocked.", productionStageId);
                }
            }

        } catch (Exception e) {
            log.error("Lỗi khi xử lý QC trigger cho routing step {}: {}",
                    event.getStepId(), e.getMessage(), e);
            throw e; // Re-throw để transaction rollback
        }
    }

    /**
     * Xử lý sự kiện inspection được approved.
     * Giải phóng QC block trên production stage và cập nhật defect count nếu FAIL.
     *
     * Sử dụng @TransactionalEventListener để đảm bảo event chỉ được xử lý
     * sau khi transaction approve hoàn thành thành công.
     *
     * @param event InspectionApprovedEvent chứa thông tin phê duyệt
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(rollbackFor = Exception.class)
    public void handleInspectionApproved(InspectionApprovedEvent event) {
        log.info("Xử lý InspectionApprovedEvent: execution={}, result={}, workOrder={}",
                event.getExecutionId(), event.getOverallResult(), event.getWorkOrderId());

        try {
            // 1. Release QC block trên production stage
            releaseQcBlock(event.getExecutionId(), event.getOverallResult());

            // 2. Nếu kết quả FAIL → cập nhật số lượng lỗi trên Work Order
            if ("fail".equals(event.getOverallResult()) && event.getWorkOrderId() != null) {
                updateWorkOrderDefectCount(event.getWorkOrderId());
            }
        } catch (Exception e) {
            log.error("Lỗi khi xử lý InspectionApprovedEvent cho execution {}: {}",
                    event.getExecutionId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Giải phóng block trên production stage khi inspection được approved.
     *
     * @param executionId ID phiên kiểm tra đã được approved
     * @param overallResult Kết quả tổng thể (pass/fail)
     */
    @Transactional(rollbackFor = Exception.class)
    public void releaseQcBlock(String executionId, String overallResult) {
        if (executionId == null || executionId.trim().isEmpty()) {
            return;
        }

        log.info("Giải phóng QC block cho execution: {}, kết quả: {}", executionId, overallResult);

        // Tìm production stage có qc_execution_id = executionId
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ProductionStage> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("qc_execution_id", executionId);

        ProductionStage productionStage = productionStageMapper.selectOne(queryWrapper);
        if (productionStage == null) {
            log.debug("Không tìm thấy production stage liên kết với execution {}. " +
                    "Có thể execution không được tạo từ routing step.", executionId);
            return;
        }

        // Release block: set qc_blocked = 0
        productionStage.setQcBlocked(0);
        productionStageMapper.updateById(productionStage);

        log.info("Đã release QC block trên production stage '{}'. Kết quả inspection: {}",
                productionStage.getId(), overallResult);
    }

    /**
     * Cập nhật số lượng lỗi trên Work Order khi inspection FAIL.
     * Giảm actualQuantity (số lượng thực tế) để phản ánh sản phẩm lỗi.
     *
     * @param workOrderId ID lệnh sản xuất
     */
    private void updateWorkOrderDefectCount(String workOrderId) {
        if (workOrderMapper == null) {
            log.warn("WorkOrderMapper không khả dụng. Không thể cập nhật số lượng lỗi cho work order: {}",
                    workOrderId);
            return;
        }

        WorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        if (workOrder == null) {
            log.warn("Không tìm thấy work order với ID: {}. Không thể cập nhật số lượng lỗi.",
                    workOrderId);
            return;
        }

        // Giảm số lượng thực tế đi 1 đơn vị (đại diện cho 1 sản phẩm lỗi)
        BigDecimal currentActual = workOrder.getActualQuantity();
        if (currentActual != null && currentActual.compareTo(BigDecimal.ZERO) > 0) {
            workOrder.setActualQuantity(currentActual.subtract(BigDecimal.ONE));
            workOrderMapper.updateById(workOrder);
            log.info("Đã cập nhật số lượng lỗi trên work order '{}'. " +
                            "Số lượng thực tế giảm từ {} xuống {}.",
                    workOrderId, currentActual, workOrder.getActualQuantity());
        } else {
            log.info("Work order '{}' có actualQuantity = {}. Ghi nhận inspection FAIL.",
                    workOrderId, currentActual);
        }
    }
}
