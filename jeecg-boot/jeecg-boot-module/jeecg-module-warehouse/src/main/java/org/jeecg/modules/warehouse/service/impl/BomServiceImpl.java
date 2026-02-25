package org.jeecg.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.warehouse.entity.Bom;
import org.jeecg.modules.warehouse.entity.BomItem;
import org.jeecg.modules.warehouse.mapper.BomItemMapper;
import org.jeecg.modules.warehouse.mapper.BomMapper;
import org.jeecg.modules.warehouse.service.BomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: BOM Service Implementation
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Service
public class BomServiceImpl extends ServiceImpl<BomMapper, Bom> implements BomService {

    @Autowired
    private BomItemMapper bomItemMapper;

    @Override
    public List<Bom> getByProductId(String productId) {
        return baseMapper.selectByProductId(productId);
    }

    @Override
    public List<Bom> getByStatus(String status) {
        return baseMapper.selectByStatus(status);
    }

    @Override
    public boolean isCodeUnique(String bomCode, String excludeId) {
        QueryWrapper<Bom> qw = new QueryWrapper<>();
        qw.eq("bom_code", bomCode);
        if (excludeId != null) {
            qw.ne("id", excludeId);
        }
        return count(qw) == 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBomWithItems(Bom bom, List<BomItem> items) {
        this.save(bom);
        if (items != null && !items.isEmpty()) {
            for (BomItem item : items) {
                item.setBomId(bom.getId());
                bomItemMapper.insert(item);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBomWithItems(Bom bom, List<BomItem> items) {
        this.updateById(bom);
        // Delete existing items and reinsert
        bomItemMapper.deleteByBomId(bom.getId());
        if (items != null && !items.isEmpty()) {
            for (BomItem item : items) {
                item.setBomId(bom.getId());
                bomItemMapper.insert(item);
            }
        }
        return true;
    }

    @Override
    public List<BomItem> getBomItems(String bomId) {
        return bomItemMapper.selectByBomId(bomId);
    }

    @Override
    public Map<String, Object> getBomDetail(String bomId) {
        Map<String, Object> result = new HashMap<>();
        Bom bom = this.getById(bomId);
        result.put("bom", bom);
        List<BomItem> items = bomItemMapper.selectByBomId(bomId);
        result.put("items", items);
        return result;
    }
}
