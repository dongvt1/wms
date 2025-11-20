package org.jeecg.modules.quartz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.quartz.entity.QuartzJob;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: Online management of scheduled tasks
 * @Author: jeecg-boot
 * @Date:  2019-01-02
 * @Version: V1.0
 */
public interface QuartzJobMapper extends BaseMapper<QuartzJob> {

    /**
     * according tojobClassNameQuery
     * @param jobClassName Task class name
     * @return
     */
	public List<QuartzJob> findByJobClassName(@Param("jobClassName") String jobClassName);

}
