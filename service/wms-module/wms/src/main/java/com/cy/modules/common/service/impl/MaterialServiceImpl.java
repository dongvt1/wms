package com.cy.modules.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.modules.common.entity.Material;
import com.cy.modules.common.entity.MaterialSubstitute;
import com.cy.modules.common.mapper.MaterialMapper;
import com.cy.modules.common.mapper.MaterialSubstituteMapper;
import com.cy.modules.common.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description: Material Service Implementation – Common Module
 * @Author: BMad
 * @Date: 2026-03-05
 * @Version: V1.0
 */
@Service
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper, Material> implements MaterialService {

    @Autowired
    private MaterialSubstituteMapper substituteMapper;

    @Override
    public IPage<Material> listWithCategory(Page<Material> page, String code, String name, String categoryId, Integer status) {
        return baseMapper.selectPageWithCategory(page, code, name, categoryId, status);
    }

    @Override
    public List<Material> listAllActive() {
        return baseMapper.selectAllActive();
    }

    @Override
    public boolean isCodeUnique(String code, String excludeId) {
        QueryWrapper<Material> qw = new QueryWrapper<>();
        qw.eq("code", code);
        if (excludeId != null) qw.ne("id", excludeId);
        return count(qw) == 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveMaterialWithSubstitutes(Material material, List<MaterialSubstitute> substitutes) {
        this.save(material);
        saveSubstituteList(material.getId(), substitutes);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMaterialWithSubstitutes(Material material, List<MaterialSubstitute> substitutes) {
        this.updateById(material);
        substituteMapper.deleteByMaterialId(material.getId());
        saveSubstituteList(material.getId(), substitutes);
        return true;
    }

    @Override
    public List<MaterialSubstitute> getSubstitutes(String materialId) {
        return substituteMapper.selectByMaterialId(materialId);
    }

    private void saveSubstituteList(String materialId, List<MaterialSubstitute> substitutes) {
        if (substitutes == null || substitutes.isEmpty()) return;
        LocalDateTime now = LocalDateTime.now();
        for (MaterialSubstitute sub : substitutes) {
            sub.setId(null); // force insert
            sub.setMaterialId(materialId);
            sub.setCreateTime(now);
            sub.setUpdateTime(now);
            substituteMapper.insert(sub);
        }
    }
}
