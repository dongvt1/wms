package com.cy.modules.planning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.modules.common.entity.Bom;
import com.cy.modules.common.entity.BomItem;
import com.cy.modules.common.entity.ProductionLog;
import com.cy.modules.common.entity.ProductionStage;
import com.cy.modules.planning.entity.WorkOrder;
import com.cy.modules.common.mapper.ProductionLogMapper;
import com.cy.modules.common.mapper.ProductionStageMapper;
import com.cy.modules.planning.mapper.WorkOrderMapper;
import lombok.extern.slf4j.Slf4j;
import com.cy.modules.common.service.BomService;
import com.cy.modules.planning.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description: Work Order Service Implementation
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Service
@Slf4j
public class WorkOrderServiceImpl extends ServiceImpl<WorkOrderMapper, WorkOrder>
        implements WorkOrderService {

    @Autowired
    private ProductionStageMapper productionStageMapper;

    @Autowired
    private ProductionLogMapper productionLogMapper;

    @Autowired
    private BomService bomService;

    @Autowired
    private com.cy.modules.warehouse.service.InventoryService inventoryService;

    @Override
    public List<WorkOrder> getByStatus(String status) {
        return baseMapper.selectByStatus(status);
    }

    @Override
    public List<WorkOrder> getByProductionLineId(String productionLineId) {
        return baseMapper.selectByProductionLineId(productionLineId);
    }

    @Override
    public boolean isCodeUnique(String orderCode, String excludeId) {
        QueryWrapper<WorkOrder> qw = new QueryWrapper<>();
        qw.eq("order_code", orderCode);
        if (excludeId != null) {
            qw.ne("id", excludeId);
        }
        return count(qw) == 0;
    }

    @Override
    public String generateOrderCode() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());

        QueryWrapper<WorkOrder> qw = new QueryWrapper<>();
        qw.likeRight("order_code", "WO" + dateStr);
        qw.orderByDesc("order_code");
        qw.last("LIMIT 1");

        WorkOrder last = this.getOne(qw);
        int seq = 1;
        if (last != null) {
            String lastCode = last.getOrderCode();
            try {
                seq = Integer.parseInt(lastCode.substring(lastCode.length() - 3)) + 1;
            } catch (NumberFormatException e) {
                seq = 1;
            }
        }
        return "WO" + dateStr + String.format("%03d", seq);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveWithStages(WorkOrder workOrder, List<ProductionStage> stages) {
        this.save(workOrder);
        if (stages != null && !stages.isEmpty()) {
            int order = 1;
            for (ProductionStage stage : stages) {
                stage.setWorkOrderId(workOrder.getId());
                stage.setStageOrder(order++);
                stage.setStatus("pending");
                stage.setCreateTime(new Date());
                stage.setUpdateTime(new Date());
                productionStageMapper.insert(stage);
            }
        }
        addLog(workOrder.getId(), null, "CREATE", null, workOrder.getCreateBy(), "Lệnh sản xuất được tạo");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String startProduction(String workOrderId, String operator) {
        WorkOrder wo = this.getById(workOrderId);
        if (wo == null) return "Không tìm thấy lệnh sản xuất";
        if (!"draft".equals(wo.getStatus()) && !"planned".equals(wo.getStatus())) {
            return "Lệnh sản xuất phải ở trạng thái draft hoặc planned để bắt đầu";
        }
        wo.setStatus("in_progress");
        wo.setActualStartDate(new Date());
        this.updateById(wo);
        addLog(workOrderId, null, "START", null, operator, "Bắt đầu sản xuất");
        return "Bắt đầu sản xuất thành công";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String completeProduction(String workOrderId, BigDecimal actualQuantity, String operator) {
        WorkOrder wo = this.getById(workOrderId);
        if (wo == null) return "Không tìm thấy lệnh sản xuất";
        if (!"in_progress".equals(wo.getStatus())) {
            return "Lệnh sản xuất phải ở trạng thái in_progress để hoàn thành";
        }

        // Get BOM to calculate material consumption
        Map<String, Object> bomDetail = bomService.getBomDetail(wo.getBomId());
        Bom bom = (Bom) bomDetail.get("bom");
        @SuppressWarnings("unchecked")
        List<BomItem> items = (List<BomItem>) bomDetail.get("items");

        if (bom == null) return "Không tìm thấy thông tin BOM";

        // Calculate ratio: actualQuantity / bom.outputQuantity
        BigDecimal ratio = actualQuantity.divide(bom.getOutputQuantity(), 6, java.math.RoundingMode.HALF_UP);

        // Deduct materials from inventory
        if (items != null) {
            for (BomItem item : items) {
                BigDecimal consumedQty = item.getQuantity().multiply(ratio).setScale(3, java.math.RoundingMode.HALF_UP);
                try {
                    String result = inventoryService.adjustInventory(
                            item.getMaterialId(),
                            -consumedQty.intValue(),
                            "Sản xuất lệnh " + wo.getOrderCode()
                    );
                    log.info("Đã trừ kho NVL {} x {}: {}", item.getMaterialId(), consumedQty, result);
                } catch (Exception e) {
                    log.error("Lỗi trừ kho NVL {}", item.getMaterialId(), e);
                    throw new RuntimeException("Lỗi trừ kho nguyên vật liệu: " + item.getMaterialId());
                }
            }
        }

        // Add finished product to inventory
        try {
            String result = inventoryService.adjustInventory(
                    bom.getProductId(),
                    actualQuantity.intValue(),
                    "Nhập kho thành phẩm lệnh " + wo.getOrderCode()
            );
            log.info("Nhập kho thành phẩm {} x {}: {}", bom.getProductId(), actualQuantity, result);
        } catch (Exception e) {
            log.error("Lỗi nhập kho thành phẩm", e);
            throw new RuntimeException("Lỗi nhập kho thành phẩm");
        }

        // Update work order
        wo.setStatus("completed");
        wo.setActualQuantity(actualQuantity);
        wo.setActualEndDate(new Date());
        this.updateById(wo);

        // Complete all pending/in_progress stages
        List<ProductionStage> stages = productionStageMapper.selectByWorkOrderId(workOrderId);
        for (ProductionStage stage : stages) {
            if (!"completed".equals(stage.getStatus())) {
                stage.setStatus("completed");
                stage.setUpdateTime(new Date());
                productionStageMapper.updateById(stage);
            }
        }

        addLog(workOrderId, null, "COMPLETE", actualQuantity, operator,
                "Hoàn thành sản xuất. Số lượng thực tế: " + actualQuantity);
        return "Hoàn thành sản xuất thành công";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String cancelWorkOrder(String workOrderId, String reason, String operator) {
        WorkOrder wo = this.getById(workOrderId);
        if (wo == null) return "Không tìm thấy lệnh sản xuất";
        if ("completed".equals(wo.getStatus()) || "cancelled".equals(wo.getStatus())) {
            return "Không thể hủy lệnh sản xuất đã hoàn thành hoặc đã hủy";
        }
        wo.setStatus("cancelled");
        this.updateById(wo);
        addLog(workOrderId, null, "CANCEL", null, operator, "Hủy lệnh: " + reason);
        return "Hủy lệnh sản xuất thành công";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateStage(String stageId, String status, BigDecimal actualDurationHours, String notes, String operator) {
        ProductionStage stage = productionStageMapper.selectById(stageId);
        if (stage == null) return "Không tìm thấy công đoạn";
        stage.setStatus(status);
        if (actualDurationHours != null) stage.setActualDurationHours(actualDurationHours);
        if (notes != null) stage.setNotes(notes);
        stage.setUpdateTime(new Date());
        productionStageMapper.updateById(stage);
        addLog(stage.getWorkOrderId(), stageId, "STAGE_UPDATE", null, operator,
                "Cập nhật công đoạn [" + stage.getStageName() + "] -> " + status);
        return "Cập nhật công đoạn thành công";
    }

    @Override
    public Map<String, Object> getWorkOrderDetail(String workOrderId) {
        Map<String, Object> result = new HashMap<>();
        WorkOrder wo = this.getById(workOrderId);
        result.put("workOrder", wo);
        List<ProductionStage> stages = productionStageMapper.selectByWorkOrderId(workOrderId);
        result.put("stages", stages);
        QueryWrapper<ProductionLog> logQw = new QueryWrapper<>();
        logQw.eq("work_order_id", workOrderId).orderByDesc("log_time");
        List<ProductionLog> logs = productionLogMapper.selectList(logQw);
        result.put("logs", logs);
        return result;
    }

    @Override
    public List<ProductionStage> getStages(String workOrderId) {
        return productionStageMapper.selectByWorkOrderId(workOrderId);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", count());
        stats.put("draftCount", count(new QueryWrapper<WorkOrder>().eq("status", "draft")));
        stats.put("plannedCount", count(new QueryWrapper<WorkOrder>().eq("status", "planned")));
        stats.put("inProgressCount", count(new QueryWrapper<WorkOrder>().eq("status", "in_progress")));
        stats.put("completedCount", count(new QueryWrapper<WorkOrder>().eq("status", "completed")));
        stats.put("cancelledCount", count(new QueryWrapper<WorkOrder>().eq("status", "cancelled")));
        return stats;
    }

    private void addLog(String workOrderId, String stageId, String action, BigDecimal quantity,
                        String operator, String notes) {
        ProductionLog log = new ProductionLog();
        log.setWorkOrderId(workOrderId);
        log.setStageId(stageId);
        log.setLogTime(new Date());
        log.setAction(action);
        log.setQuantity(quantity);
        log.setOperator(operator);
        log.setNotes(notes);
        productionLogMapper.insert(log);
    }
}
