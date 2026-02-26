package org.jeecg.modules.system.mapper;

import java.util.List;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserPosition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.system.vo.SysUserPositionVo;

/**
 * @Description: User position relationship table
 * @Author: jeecg-boot
 * @Date:   2023-02-14
 * @Version: V1.0
 */
public interface SysUserPositionMapper extends BaseMapper<SysUserPosition> {

    /**
     * Get a list of job users
     * @param page
     * @param positionId
     * @return
     */
    List<SysUser> getPositionUserList(@Param("page") Page<SysUser> page, @Param("positionId") String positionId);

    /**
     * Get whether the member exists in the position
     * @param userId
     * @param positionId
     * @return
     */
    @Select("SELECT count(*) FROM sys_user_position WHERE user_id = #{userId} and position_id = #{positionId}")
    Long getUserPositionCount(@Param("userId") String userId, @Param("positionId") String positionId);

    /**
     * by positionid删除User position relationship table
     * @param positionId
     */
    @Delete("DELETE FROM sys_user_position WHERE position_id = #{positionId} ")
    void removeByPositionId(@Param("positionId") String positionId);

    /**
     * Remove members from job list
     * @param userIdList
     * @param positionId
     */
    void removePositionUser(@Param("userIdList") List<String> userIdList, @Param("positionId") String positionId);

    /**
     * According to useridSearch for jobsid
     * @param userId
     * @return
     */
    List<String> getPositionIdByUserId(@Param("userId") String userId);


    /**
     * According to userIDand tenantsIDGet jobid
     * @param userId
     * @param tenantId
     * @return
     */
    @InterceptorIgnore(tenantLine = "true")
    List<String> getPositionIdByUserTenantId(@Param("userId")String userId, @Param("tenantId")Integer tenantId);

    /**
     * According to useridGet user position
     * @param userIdList
     * @param tenantId
     * @return
     */
    List<SysUserPositionVo> getPositionIdByUsersTenantId(@Param("userIdList") List<SysUser> userIdList, @Param("tenantId") Integer tenantId);

    /**
     * 根据职位名称and tenantsid，删除User position relationship table
     * @param positionNames
     * @param tenantId
     * @param userId
     */
    void deleteUserPosByNameAndTenantId(@Param("positionNames") List<String> positionNames, @Param("tenantId") Integer tenantId, @Param("userId") String userId);
}
