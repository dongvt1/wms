package org.jeecg.modules.qms.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.qms.entity.QmsChecklistItem;

/**
 * @Description: QMS Checklist Item Mapper
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface QmsChecklistItemMapper extends BaseMapper<QmsChecklistItem> {

    List<QmsChecklistItem> selectByTemplateId(@Param("templateId") String templateId);
}
