package org.jeecg.modules.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.system.entity.SysDepartRoleUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: Department role personnel information
 * @Author: jeecg-boot
 * @Date:   2020-02-13
 * @Version: V1.0
 */
public interface SysDepartRoleUserMapper extends BaseMapper<SysDepartRoleUser> {

    void deleteByRoleIds(@Param("ids")List<String> ids);
}
