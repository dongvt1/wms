package com.cy.modules.planning.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.planning.entity.RoutingStep;

/**
 * @Description: Routing Step Mapper
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface RoutingStepMapper extends BaseMapper<RoutingStep> {

    List<RoutingStep> selectByRoutingId(@Param("routingId") String routingId);

    int deleteByRoutingId(@Param("routingId") String routingId);
}
