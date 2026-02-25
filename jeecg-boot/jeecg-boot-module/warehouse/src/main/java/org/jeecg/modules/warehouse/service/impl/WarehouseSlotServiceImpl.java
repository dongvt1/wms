package org.jeecg.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.warehouse.entity.WarehouseSlot;
import org.jeecg.modules.warehouse.mapper.WarehouseSlotMapper;
import org.jeecg.modules.warehouse.service.WarehouseSlotService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: vị trí trong kho
 * @Author: BMad
 * @Date:   2025-11-17
 * @Version: V1.0
 */
@Slf4j
@Service
public class WarehouseSlotServiceImpl extends ServiceImpl<WarehouseSlotMapper, WarehouseSlot> implements WarehouseSlotService {

    @Override
    public List<WarehouseSlot> getByShelfId(String shelfId) {
        return baseMapper.selectByShelfId(shelfId);
    }

    @Override
    public List<WarehouseSlot> getByStatus(Integer status) {
        return baseMapper.selectByStatus(status);
    }

    @Override
    public List<WarehouseSlot> getWithShelfAndAreaInfo() {
        return baseMapper.selectWithShelfAndAreaInfo();
    }

    @Override
    public List<WarehouseSlot> getEmptySlots() {
        return baseMapper.selectEmptySlots();
    }

    @Override
    public boolean assignProduct(String id, String productCode) {
        try {
            return baseMapper.assignProduct(id, productCode, 1) > 0;
        } catch (Exception e) {
            log.error("Gán sản phẩm vào vị trí thất bại", e);
            return false;
        }
    }

    @Override
    public boolean removeProduct(String id) {
        try {
            return baseMapper.removeProduct(id) > 0;
        } catch (Exception e) {
            log.error("Xóa sản phẩm khỏi vị trí thất bại", e);
            return false;
        }
    }

    @Override
    public boolean moveProduct(String fromId, String toId) {
        try {
            return baseMapper.moveProduct(fromId, toId) > 0;
        } catch (Exception e) {
            log.error("Di chuyển sản phẩm giữa các vị trí thất bại", e);
            return false;
        }
    }

    @Override
    public List<WarehouseSlot> findByProductCode(String productCode) {
        QueryWrapper<WarehouseSlot> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_code", productCode);
        return list(queryWrapper);
    }
}