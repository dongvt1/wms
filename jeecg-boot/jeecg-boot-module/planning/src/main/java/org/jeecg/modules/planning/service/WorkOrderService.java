package org.jeecg.modules.planning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.planning.entity.ProductionStage;
import org.jeecg.modules.planning.entity.WorkOrder;

import java.util.List;
import java.util.Map;

/**
 * @Description: Work Order Service
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface WorkOrderService extends IService<WorkOrder> {

    List<WorkOrder> getByStatus(String status);

    List<WorkOrder> getByProductionLineId(String productionLineId);

    boolean isCodeUnique(String orderCode, String excludeId);

    String generateOrderCode();

    /**
     * Save work order with production stages
     */
    boolean saveWithStages(WorkOrder workOrder, List<ProductionStage> stages);

    /**
     * Start production: change status to in_progress, record actual start date
     */
    String startProduction(String workOrderId, String operator);

    /**
     * Complete production: deduct BOM materials from inventory, add finished product stock
     */
    String completeProduction(String workOrderId, java.math.BigDecimal actualQuantity, String operator);

    /**
     * Cancel work order
     */
    String cancelWorkOrder(String workOrderId, String reason, String operator);

    /**
     * Update a specific production stage
     */
    String updateStage(String stageId, String status, java.math.BigDecimal actualDurationHours, String notes, String operator);

    /**
     * Get work order detail with stages and logs
     */
    Map<String, Object> getWorkOrderDetail(String workOrderId);

    /**
     * Get production stages by work order
     */
    List<ProductionStage> getStages(String workOrderId);

    /**
     * Get production statistics dashboard
     */
    Map<String, Object> getStatistics();
}
