package org.jeecg.modules.qms.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.qms.entity.QmsChecklistTemplate;

/**
 * @Description: QMS Checklist Template Mapper
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface QmsChecklistTemplateMapper extends BaseMapper<QmsChecklistTemplate> {

    List<QmsChecklistTemplate> selectByInspectionType(@Param("inspectionType") String inspectionType);

    List<QmsChecklistTemplate> selectByProductId(@Param("productId") String productId);
}
