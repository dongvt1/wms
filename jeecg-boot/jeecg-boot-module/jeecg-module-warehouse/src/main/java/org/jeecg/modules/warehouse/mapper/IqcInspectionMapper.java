package org.jeecg.modules.warehouse.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.warehouse.entity.IqcInspection;

/**
 * @Description: IQC Inspection Mapper
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface IqcInspectionMapper extends BaseMapper<IqcInspection> {

    List<IqcInspection> selectByProductId(@Param("productId") String productId);

    List<IqcInspection> selectByStatus(@Param("status") String status);
}
