package com.cy.modules.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.modules.common.entity.Material;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: Material Mapper – Common Module
 * @Author: BMad
 * @Date: 2026-03-05
 */
public interface MaterialMapper extends BaseMapper<Material> {

    /** Tìm kiếm vật tư có phân trang */
    IPage<Material> selectPageWithCategory(Page<Material> page,
                                           @Param("code") String code,
                                           @Param("name") String name,
                                           @Param("categoryId") String categoryId,
                                           @Param("status") Integer status);

    /** Lấy tất cả vật tư đang active (cho dropdown) */
    List<Material> selectAllActive();
}
