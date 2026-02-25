package org.jeecg.modules.planning.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.planning.entity.Bom;

/**
 * @Description: BOM Mapper
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface BomMapper extends BaseMapper<Bom> {

    List<Bom> selectByProductId(@Param("productId") String productId);

    List<Bom> selectByStatus(@Param("status") String status);
}
