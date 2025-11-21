package org.jeecg.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.warehouse.entity.WarehouseArea;
import org.jeecg.modules.warehouse.mapper.WarehouseAreaMapper;
import org.jeecg.modules.warehouse.service.WarehouseAreaService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: Warehouse Area
 * @Author: BMad
 * @Date:   2025-11-17
 * @Version: V1.0
 */
@Slf4j
@Service
public class WarehouseAreaServiceImpl extends ServiceImpl<WarehouseAreaMapper, WarehouseArea> implements WarehouseAreaService {

    @Override
    public List<WarehouseArea> getByStatus(Integer status) {
        return baseMapper.selectByStatus(status);
    }

    @Override
    public boolean updateUsedCapacity(String id, Integer usedCapacity) {
        try {
            return baseMapper.updateUsedCapacity(id, usedCapacity) > 0;
        } catch (Exception e) {
            log.error("Failed to update used capacity", e);
            return false;
        }
    }

    @Override
    public boolean increaseUsedCapacity(String id) {
        try {
            WarehouseArea area = getById(id);
            if (area != null) {
                Integer newUsedCapacity = area.getUsedCapacity() + 1;
                return updateUsedCapacity(id, newUsedCapacity);
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to increase used capacity", e);
            return false;
        }
    }

    @Override
    public boolean decreaseUsedCapacity(String id) {
        try {
            WarehouseArea area = getById(id);
            if (area != null && area.getUsedCapacity() > 0) {
                Integer newUsedCapacity = area.getUsedCapacity() - 1;
                return updateUsedCapacity(id, newUsedCapacity);
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to decrease used capacity", e);
            return false;
        }
    }
}