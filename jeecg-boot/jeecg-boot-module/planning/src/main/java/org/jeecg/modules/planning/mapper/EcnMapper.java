package org.jeecg.modules.planning.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.planning.entity.Ecn;

/**
 * @Description: ECN Mapper
 * @Author: BMad
 * @Date: 2026-02-26
 */
public interface EcnMapper extends BaseMapper<Ecn> {

    List<Ecn> selectByBomId(@Param("bomId") String bomId);

    List<Ecn> selectByStatus(@Param("status") String status);
}
