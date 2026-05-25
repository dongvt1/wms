package com.cy.modules.planning.agent.client;

import com.cy.modules.planning.agent.dto.BomStructure;
import com.cy.modules.planning.agent.dto.DispatchNotification;
import com.cy.modules.planning.agent.dto.InventorySnapshot;
import com.cy.modules.planning.agent.dto.MaterialIssuanceRequest;
import com.cy.modules.planning.agent.dto.ProductionLineCapacity;
import com.cy.modules.planning.agent.dto.ProductionOrderRequest;
import com.cy.modules.planning.agent.dto.SupplierLeadTime;
import com.cy.modules.planning.agent.dto.WarehouseReceiptRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * Client interface cho tích hợp với hệ thống ERP-MRP-WMS.
 * Cung cấp khả năng truy vấn tồn kho, BOM, năng lực sản xuất,
 * và thực hiện các thao tác lệnh sản xuất, xuất kho, nhập kho, giao hàng.
 */
public interface ErpClient {

    /**
     * Lấy mức tồn kho hiện tại cho danh sách vật tư.
     *
     * @param materialIds danh sách mã vật tư cần kiểm tra
     * @return snapshot tồn kho
     */
    InventorySnapshot getInventoryLevels(List<String> materialIds);

    /**
     * Lấy cấu trúc BOM cho sản phẩm.
     *
     * @param productId mã sản phẩm
     * @return cấu trúc BOM
     */
    BomStructure getBom(String productId);

    /**
     * Lấy thời gian giao hàng của nhà cung cấp cho danh sách vật tư.
     *
     * @param materialIds danh sách mã vật tư
     * @return danh sách thời gian giao hàng theo nhà cung cấp
     */
    List<SupplierLeadTime> getSupplierLeadTimes(List<String> materialIds);

    /**
     * Lấy năng lực sản xuất của dây chuyền trong khoảng thời gian.
     *
     * @param lineId mã dây chuyền sản xuất
     * @param from   ngày bắt đầu
     * @param to     ngày kết thúc
     * @return năng lực sản xuất
     */
    ProductionLineCapacity getLineCapacity(String lineId, LocalDate from, LocalDate to);

    /**
     * Tạo lệnh sản xuất trong ERP.
     *
     * @param request thông tin lệnh sản xuất
     */
    void createProductionOrder(ProductionOrderRequest request);

    /**
     * Kích hoạt xuất kho nguyên vật liệu theo BOM.
     *
     * @param request thông tin yêu cầu xuất kho
     */
    void triggerMaterialIssuance(MaterialIssuanceRequest request);

    /**
     * Ghi nhận nhập kho thành phẩm.
     *
     * @param request thông tin nhập kho
     */
    void recordWarehouseReceipt(WarehouseReceiptRequest request);

    /**
     * Thông báo giao hàng cho kho bán hàng.
     *
     * @param notification thông tin giao hàng
     */
    void notifyDispatch(DispatchNotification notification);
}
