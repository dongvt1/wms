package org.jeecg.modules.planning.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.planning.entity.ItemMaster;

/**
 * @Description: Item Master Mapper
 * @Author: BMad
 * @Date: 2026-02-26
 */
public interface ItemMasterMapper extends BaseMapper<ItemMaster> {

    List<ItemMaster> searchByMpn(@Param("mpn") String mpn);

    List<ItemMaster> searchByIpn(@Param("ipn") String ipn);

    List<ItemMaster> selectByLifecycle(@Param("lifecycleStatus") String lifecycleStatus);

    List<ItemMaster> selectByCategory(@Param("category") String category);
}
