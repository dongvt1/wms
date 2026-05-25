package com.cy.modules.planning.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cy.modules.planning.agent.entity.PlanningOrder;
import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.enums.ValidationStatus;
import com.cy.modules.planning.agent.event.OrdersReceivedEvent;
import com.cy.modules.planning.agent.mapper.PlanningOrderMapper;
import com.cy.modules.planning.agent.service.OrderIngestionService;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: Triển khai xử lý và xác thực đơn hàng đầu vào
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class OrderIngestionServiceImpl implements OrderIngestionService {

    @Resource
    private PlanningOrderMapper planningOrderMapper;

    @Resource
    private PlanningNotificationService planningNotificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Lắng nghe event OrdersReceivedEvent từ OrderSyncService.
     * Khi có đơn hàng mới được đồng bộ, tự động xử lý xác thực và phân loại.
     */
    @EventListener
    public void onOrdersReceived(OrdersReceivedEvent event) {
        log.info("[OrderIngestion] Nhận event với {} đơn hàng mới", event.getNewOrderIds().size());
        processNewOrders(event.getNewOrderIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processNewOrders(List<String> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            log.warn("[OrderIngestion] Danh sách đơn hàng rỗng, bỏ qua");
            return;
        }

        log.info("[OrderIngestion] Bắt đầu xử lý {} đơn hàng", orderIds.size());

        List<String> incompleteOrderIds = new ArrayList<>();
        List<String> invalidOrderIds = new ArrayList<>();

        for (String orderId : orderIds) {
            PlanningOrder order = planningOrderMapper.selectById(orderId);
            if (order == null) {
                log.warn("[OrderIngestion] Không tìm thấy đơn hàng với ID: {}", orderId);
                continue;
            }

            // Bước 1: Kiểm tra tính đầy đủ (completeness)
            List<String> missingFields = checkCompleteness(order);
            if (!missingFields.isEmpty()) {
                markAsIncomplete(order, missingFields);
                incompleteOrderIds.add(orderId);
                continue;
            }

            // Bước 2: Kiểm tra tính hợp lệ (validity)
            Map<String, String> invalidFields = checkValidity(order);
            if (!invalidFields.isEmpty()) {
                markAsInvalid(order, invalidFields);
                invalidOrderIds.add(orderId);
                continue;
            }

            // Bước 3: Đánh dấu hợp lệ
            order.setValidationStatus(ValidationStatus.VALID.getValue());
            order.setValidationErrors(null);
            planningOrderMapper.updateById(order);
            log.debug("[OrderIngestion] Đơn hàng {} hợp lệ", orderId);
        }

        // Bước 4: Tính lại priority_rank cho tất cả đơn hàng valid + pending
        recalculatePriorityRanks();

        // Bước 5: Gửi thông báo cho đơn hàng incomplete/invalid
        notifyForProblematicOrders(incompleteOrderIds, invalidOrderIds);

        log.info("[OrderIngestion] Hoàn tất xử lý: {} incomplete, {} invalid",
                incompleteOrderIds.size(), invalidOrderIds.size());
    }

    @Override
    public List<PlanningOrder> getPrioritizedOrderQueue() {
        LambdaQueryWrapper<PlanningOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlanningOrder::getValidationStatus, ValidationStatus.VALID.getValue())
                .eq(PlanningOrder::getStatus, "pending")
                .orderByAsc(PlanningOrder::getDeadline)
                .orderByAsc(PlanningOrder::getReceiptTimestamp);
        return planningOrderMapper.selectList(wrapper);
    }

    @Override
    public Map<String, List<PlanningOrder>> getOrdersGroupedByProductType() {
        List<PlanningOrder> validOrders = getPrioritizedOrderQueue();
        return validOrders.stream()
                .collect(Collectors.groupingBy(
                        PlanningOrder::getProductType,
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    list.sort(Comparator.comparing(PlanningOrder::getDeadline)
                                            .thenComparing(PlanningOrder::getReceiptTimestamp));
                                    return list;
                                }
                        )
                ));
    }

    /**
     * Kiểm tra tính đầy đủ: product_type, customer_name, quantity, deadline phải non-null/non-empty.
     *
     * @return danh sách tên trường bị thiếu
     */
    private List<String> checkCompleteness(PlanningOrder order) {
        List<String> missingFields = new ArrayList<>();

        if (order.getProductType() == null || order.getProductType().trim().isEmpty()) {
            missingFields.add("product_type");
        }
        if (order.getCustomerName() == null || order.getCustomerName().trim().isEmpty()) {
            missingFields.add("customer_name");
        }
        if (order.getQuantity() == null) {
            missingFields.add("quantity");
        }
        if (order.getDeadline() == null) {
            missingFields.add("deadline");
        }

        return missingFields;
    }

    /**
     * Kiểm tra tính hợp lệ: quantity > 0, deadline >= today.
     *
     * @return map với key là tên trường, value là mô tả lỗi
     */
    private Map<String, String> checkValidity(PlanningOrder order) {
        Map<String, String> invalidFields = new LinkedHashMap<>();

        // Kiểm tra quantity > 0
        if (order.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            invalidFields.put("quantity", "Số lượng phải lớn hơn 0, giá trị hiện tại: " + order.getQuantity());
        }

        // Kiểm tra deadline >= today
        LocalDate today = LocalDate.now();
        LocalDate deadlineDate = order.getDeadline().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        if (deadlineDate.isBefore(today)) {
            invalidFields.put("deadline", "Hạn giao hàng không được trong quá khứ, giá trị hiện tại: " + deadlineDate);
        }

        return invalidFields;
    }

    /**
     * Đánh dấu đơn hàng là incomplete và lưu chi tiết lỗi.
     */
    private void markAsIncomplete(PlanningOrder order, List<String> missingFields) {
        order.setValidationStatus(ValidationStatus.INCOMPLETE.getValue());
        order.setPriorityRank(null);

        Map<String, Object> errors = new LinkedHashMap<>();
        errors.put("type", "incomplete");
        errors.put("missing_fields", missingFields);
        errors.put("message", "Đơn hàng thiếu thông tin: " + String.join(", ", missingFields));

        order.setValidationErrors(toJson(errors));
        planningOrderMapper.updateById(order);

        log.warn("[OrderIngestion] Đơn hàng {} thiếu thông tin: {}", order.getId(), missingFields);
    }

    /**
     * Đánh dấu đơn hàng là invalid và lưu chi tiết lỗi.
     */
    private void markAsInvalid(PlanningOrder order, Map<String, String> invalidFields) {
        order.setValidationStatus(ValidationStatus.INVALID.getValue());
        order.setPriorityRank(null);

        Map<String, Object> errors = new LinkedHashMap<>();
        errors.put("type", "invalid");
        errors.put("invalid_fields", invalidFields);
        errors.put("message", "Đơn hàng không hợp lệ: " + String.join(", ", invalidFields.keySet()));

        order.setValidationErrors(toJson(errors));
        planningOrderMapper.updateById(order);

        log.warn("[OrderIngestion] Đơn hàng {} không hợp lệ: {}", order.getId(), invalidFields);
    }

    /**
     * Tính lại priority_rank cho tất cả đơn hàng valid + pending.
     * Sắp xếp theo deadline ASC, receipt_timestamp ASC.
     * Gán priority_rank = 1, 2, 3, ... theo thứ tự.
     */
    private void recalculatePriorityRanks() {
        List<PlanningOrder> validPendingOrders = getPrioritizedOrderQueue();

        int rank = 1;
        for (PlanningOrder order : validPendingOrders) {
            if (!Integer.valueOf(rank).equals(order.getPriorityRank())) {
                order.setPriorityRank(rank);
                planningOrderMapper.updateById(order);
            }
            rank++;
        }

        log.info("[OrderIngestion] Đã cập nhật priority_rank cho {} đơn hàng", validPendingOrders.size());
    }

    /**
     * Gửi thông báo cho quản lý sản xuất về đơn hàng incomplete/invalid.
     */
    private void notifyForProblematicOrders(List<String> incompleteOrderIds, List<String> invalidOrderIds) {
        if (!incompleteOrderIds.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("order_ids", incompleteOrderIds);
            data.put("count", incompleteOrderIds.size());

            planningNotificationService.notifyProductionManager(
                    NotificationType.ORDER_INCOMPLETE,
                    String.format("Có %d đơn hàng thiếu thông tin cần xử lý", incompleteOrderIds.size()),
                    data
            );
            log.info("[OrderIngestion] Đã gửi thông báo cho {} đơn hàng incomplete", incompleteOrderIds.size());
        }

        if (!invalidOrderIds.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("order_ids", invalidOrderIds);
            data.put("count", invalidOrderIds.size());

            planningNotificationService.notifyProductionManager(
                    NotificationType.ORDER_INVALID,
                    String.format("Có %d đơn hàng không hợp lệ cần xử lý", invalidOrderIds.size()),
                    data
            );
            log.info("[OrderIngestion] Đã gửi thông báo cho {} đơn hàng invalid", invalidOrderIds.size());
        }
    }

    /**
     * Chuyển đổi object sang JSON string.
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("[OrderIngestion] Lỗi chuyển đổi JSON: {}", e.getMessage(), e);
            return "{}";
        }
    }
}
