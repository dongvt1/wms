package com.cy.modules.planning.agent.service;

import com.cy.modules.planning.agent.entity.PurchaseRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service interface cho phối hợp mua sắm nguyên vật liệu.
 * Tạo Purchase Request, sinh phương án thay thế khi lead time vượt deadline,
 * và cập nhật trạng thái khi nhận hàng.
 *
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 */
public interface ProcurementCoordinationService {

    /**
     * Tạo Purchase Request cho nguyên vật liệu thiếu hụt.
     * Tính required_delivery_date = productionStartDate - supplier_lead_time.
     * Nếu required_delivery_date < today → sinh phương án thay thế.
     *
     * @param orderId             ID đơn hàng kế hoạch
     * @param materialId          Mã nguyên vật liệu
     * @param deficitQty          Số lượng thiếu hụt
     * @param productionStartDate Ngày bắt đầu sản xuất dự kiến
     * @return PurchaseRequest đã được lưu
     */
    PurchaseRequest generatePurchaseRequest(String orderId, String materialId, BigDecimal deficitQty, LocalDate productionStartDate);

    /**
     * Sinh ít nhất 2 phương án thay thế khi lead time vượt deadline.
     * Các phương án bao gồm: vận chuyển nhanh, nhà cung cấp thay thế, điều chỉnh lịch sản xuất.
     * Mỗi phương án có: loại, chi phí ước tính, ngày giao hàng sửa đổi, mô tả.
     *
     * @param orderId  ID đơn hàng kế hoạch
     * @param deadline Hạn giao hàng của đơn hàng
     * @return danh sách phương án thay thế dưới dạng JSON string
     */
    String generateAlternatives(String orderId, LocalDate deadline);

    /**
     * Xử lý khi nguyên vật liệu được nhận vào kho.
     * Cập nhật MaterialAvailability, trạng thái PurchaseRequest,
     * tính lại khả thi sản xuất, và cập nhật supplier lead time database.
     *
     * @param materialId  Mã nguyên vật liệu
     * @param receivedQty Số lượng nhận được
     */
    void onMaterialReceived(String materialId, BigDecimal receivedQty);

    /**
     * Lấy danh sách Purchase Request theo đơn hàng.
     *
     * @param orderId ID đơn hàng
     * @return danh sách PR
     */
    List<PurchaseRequest> getPurchaseRequestsByOrder(String orderId);
}
