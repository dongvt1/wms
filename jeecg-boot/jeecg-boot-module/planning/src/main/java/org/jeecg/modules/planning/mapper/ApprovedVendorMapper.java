package org.jeecg.modules.planning.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.planning.entity.ApprovedVendor;

/**
 * @Description: Approved Vendor Mapper
 * @Author: BMad
 * @Date: 2026-02-26
 */
public interface ApprovedVendorMapper extends BaseMapper<ApprovedVendor> {

    List<ApprovedVendor> selectByItemMasterId(@Param("itemMasterId") String itemMasterId);
}
