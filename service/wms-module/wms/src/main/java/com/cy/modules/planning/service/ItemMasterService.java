package com.cy.modules.planning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.modules.planning.entity.ItemMaster;

import java.util.List;
import java.util.Map;

/**
 * @Description: Item Master Service
 * @Author: BMad
 * @Date: 2026-02-26
 */
public interface ItemMasterService extends IService<ItemMaster> {

    List<ItemMaster> searchByMpn(String mpn);

    List<ItemMaster> searchByIpn(String ipn);

    List<ItemMaster> getByLifecycle(String lifecycleStatus);

    List<ItemMaster> getByCategory(String category);

    boolean isIpnUnique(String ipn, String excludeId);

    /**
     * Lấy danh sách nhà sản xuất + nhà cung cấp được phê duyệt (AML + AVL)
     */
    Map<String, Object> getAlternatives(String itemMasterId);
}
