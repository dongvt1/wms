package org.jeecg.modules.planning.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.planning.entity.BomItemRefDes;

/**
 * @Description: BOM Item RefDes Mapper
 * @Author: BMad
 * @Date: 2026-02-26
 */
public interface BomItemRefDesMapper extends BaseMapper<BomItemRefDes> {

    List<BomItemRefDes> selectByBomItemId(@Param("bomItemId") String bomItemId);

    int deleteByBomItemId(@Param("bomItemId") String bomItemId);
}
