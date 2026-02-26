package com.cy.modules.planning.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.planning.entity.EcnApproval;

/**
 * @Description: ECN Approval Mapper
 * @Author: BMad
 * @Date: 2026-02-26
 */
public interface EcnApprovalMapper extends BaseMapper<EcnApproval> {

    List<EcnApproval> selectByEcnId(@Param("ecnId") String ecnId);
}
