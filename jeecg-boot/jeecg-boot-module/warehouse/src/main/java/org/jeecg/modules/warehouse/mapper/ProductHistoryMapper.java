package org.jeecg.modules.warehouse.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.warehouse.entity.ProductHistory;

/**
 * @Description: Product History Mapper
 * @Author: BMad
 * @Date:   2025-11-20
 * @Version: V1.0
 */
public interface ProductHistoryMapper extends BaseMapper<ProductHistory> {

    /**
     * Get product history by product ID
     * @param productId Product ID
     * @return List of product history
     */
    List<ProductHistory> selectByProductId(@Param("productId") String productId);

    /**
     * Get product history by user ID
     * @param userId User ID
     * @return List of product history
     */
    List<ProductHistory> selectByUserId(@Param("userId") String userId);

    /**
     * Get product history by action
     * @param action Action type (CREATE, UPDATE, DELETE)
     * @return List of product history
     */
    List<ProductHistory> selectByAction(@Param("action") String action);
}