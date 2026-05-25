package com.cy.modules.planning.agent.service;

import com.cy.modules.planning.agent.dto.BomStructure;
import com.cy.modules.planning.agent.dto.InventorySnapshot;
import com.cy.modules.planning.agent.dto.SupplierLeadTime;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface cho đồng bộ dữ liệu tồn kho từ ERP-MRP-WMS.
 * Polling mỗi 15 phút, cache dữ liệu BOM, lead time nhà cung cấp, và mức tồn kho trong Redis.
 * Theo dõi trạng thái đồng bộ qua bảng ap_sync_status.
 */
public interface InventorySyncService {

    /**
     * Thực hiện đồng bộ toàn bộ dữ liệu tồn kho từ ERP-MRP-WMS.
     * Bao gồm: mức tồn kho, BOM, và thời gian giao hàng nhà cung cấp.
     * Có retry logic: 3 lần thử với exponential backoff (1s, 2s, 4s).
     */
    void syncInventoryData();

    /**
     * Lấy mức tồn kho đã cache cho một vật tư.
     *
     * @param materialId mã vật tư
     * @return mức tồn kho khả dụng, null nếu không có trong cache
     */
    BigDecimal getInventoryLevel(String materialId);

    /**
     * Lấy cấu trúc BOM đã cache cho một sản phẩm.
     *
     * @param productId mã sản phẩm
     * @return cấu trúc BOM, null nếu không có trong cache
     */
    BomStructure getBom(String productId);

    /**
     * Lấy thời gian giao hàng nhà cung cấp đã cache cho một vật tư.
     *
     * @param materialId mã vật tư
     * @return danh sách thời gian giao hàng theo nhà cung cấp, empty list nếu không có
     */
    List<SupplierLeadTime> getSupplierLeadTime(String materialId);
}
