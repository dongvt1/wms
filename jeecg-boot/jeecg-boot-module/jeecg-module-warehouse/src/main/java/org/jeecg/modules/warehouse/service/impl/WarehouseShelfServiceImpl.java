package org.jeecg.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.warehouse.entity.WarehouseShelf;
import org.jeecg.modules.warehouse.mapper.WarehouseShelfMapper;
import org.jeecg.modules.warehouse.service.IWarehouseShelfService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: kệ kho
 * @Author: BMad
 * @Date:   2025-11-17
 * @Version: V1.0
 */
@Slf4j
@Service
public class WarehouseShelfServiceImpl extends ServiceImpl<WarehouseShelfMapper, WarehouseShelf> implements IWarehouseShelfService {

    @Override
    public List<WarehouseShelf> getByAreaId(String areaId) {
        return baseMapper.selectByAreaId(areaId);
    }

    @Override
    public List<WarehouseShelf> getByStatus(Integer status) {
        return baseMapper.selectByStatus(status);
    }

    @Override
    public List<WarehouseShelf> getWithAreaInfo() {
        return baseMapper.selectWithAreaInfo();
    }

    @Override
    public boolean updateUsedCapacity(String id, Integer usedCapacity) {
        try {
            return baseMapper.updateUsedCapacity(id, usedCapacity) > 0;
        } catch (Exception e) {
            log.error("Cập nhật số vị trí đã sử dụng thất bại", e);
            return false;
        }
    }

    @Override
    public boolean increaseUsedCapacity(String id) {
        try {
            WarehouseShelf shelf = getById(id);
            if (shelf != null) {
                Integer newUsedCapacity = shelf.getUsedCapacity() + 1;
                return updateUsedCapacity(id, newUsedCapacity);
            }
            return false;
        } catch (Exception e) {
            log.error("Tăng số vị trí đã sử dụng thất bại", e);
            return false;
        }
    }

    @Override
    public boolean decreaseUsedCapacity(String id) {
        try {
            WarehouseShelf shelf = getById(id);
            if (shelf != null && shelf.getUsedCapacity() > 0) {
                Integer newUsedCapacity = shelf.getUsedCapacity() - 1;
                return updateUsedCapacity(id, newUsedCapacity);
            }
            return false;
        } catch (Exception e) {
            log.error("Giảm số vị trí đã sử dụng thất bại", e);
            return false;
        }
    }
}