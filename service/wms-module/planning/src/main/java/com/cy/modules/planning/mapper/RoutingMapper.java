package com.cy.modules.planning.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.planning.entity.Routing;

/**
 * @Description: Routing Mapper
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface RoutingMapper extends BaseMapper<Routing> {

    List<Routing> selectByProductId(@Param("productId") String productId);

    List<Routing> selectByStatus(@Param("status") String status);
}
