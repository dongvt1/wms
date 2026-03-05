package com.cy.modules.common.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.modules.common.entity.Material;
import com.cy.modules.common.entity.MaterialSubstitute;

import java.util.List;

/**
 * @Description: Material Service – Common Module
 * @Author: BMad
 * @Date: 2026-03-05
 * @Version: V1.0
 */
public interface MaterialService extends IService<Material> {

    /** Danh sách vật tư có phân trang + search */
    IPage<Material> listWithCategory(Page<Material> page, String code, String name, String categoryId, Integer status);

    /** Lấy tất cả vật tư active (cho dropdown) */
    List<Material> listAllActive();

    /** Kiểm tra mã vật tư duy nhất */
    boolean isCodeUnique(String code, String excludeId);

    /** Lưu vật tư kèm danh sách linh kiện thay thế */
    boolean saveMaterialWithSubstitutes(Material material, List<MaterialSubstitute> substitutes);

    /** Cập nhật vật tư kèm danh sách linh kiện thay thế */
    boolean updateMaterialWithSubstitutes(Material material, List<MaterialSubstitute> substitutes);

    /** Lấy danh sách linh kiện thay thế theo vật tư */
    List<MaterialSubstitute> getSubstitutes(String materialId);
}
