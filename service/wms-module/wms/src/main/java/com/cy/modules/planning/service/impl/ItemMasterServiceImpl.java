package com.cy.modules.planning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.modules.planning.entity.ApprovedManufacturer;
import com.cy.modules.planning.entity.ApprovedVendor;
import com.cy.modules.planning.entity.ItemMaster;
import com.cy.modules.planning.mapper.ApprovedManufacturerMapper;
import com.cy.modules.planning.mapper.ApprovedVendorMapper;
import com.cy.modules.planning.mapper.ItemMasterMapper;
import com.cy.modules.planning.service.ItemMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: Item Master Service Implementation
 * @Author: BMad
 * @Date: 2026-02-26
 */
@Service
public class ItemMasterServiceImpl extends ServiceImpl<ItemMasterMapper, ItemMaster> implements ItemMasterService {

    @Autowired
    private ApprovedManufacturerMapper approvedManufacturerMapper;

    @Autowired
    private ApprovedVendorMapper approvedVendorMapper;

    @Override
    public List<ItemMaster> searchByMpn(String mpn) {
        return baseMapper.searchByMpn(mpn);
    }

    @Override
    public List<ItemMaster> searchByIpn(String ipn) {
        return baseMapper.searchByIpn(ipn);
    }

    @Override
    public List<ItemMaster> getByLifecycle(String lifecycleStatus) {
        return baseMapper.selectByLifecycle(lifecycleStatus);
    }

    @Override
    public List<ItemMaster> getByCategory(String category) {
        return baseMapper.selectByCategory(category);
    }

    @Override
    public boolean isIpnUnique(String ipn, String excludeId) {
        QueryWrapper<ItemMaster> qw = new QueryWrapper<>();
        qw.eq("ipn", ipn);
        if (excludeId != null) {
            qw.ne("id", excludeId);
        }
        return count(qw) == 0;
    }

    @Override
    public Map<String, Object> getAlternatives(String itemMasterId) {
        Map<String, Object> result = new HashMap<>();
        ItemMaster item = this.getById(itemMasterId);
        result.put("itemMaster", item);

        // AML – nhà sản xuất được phê duyệt, sắp xếp theo priority
        List<ApprovedManufacturer> manufacturers = approvedManufacturerMapper.selectByItemMasterId(itemMasterId);
        result.put("approvedManufacturers", manufacturers);

        // AVL – nhà cung cấp được phê duyệt, sắp xếp theo priority
        List<ApprovedVendor> vendors = approvedVendorMapper.selectByItemMasterId(itemMasterId);
        result.put("approvedVendors", vendors);

        return result;
    }
}
