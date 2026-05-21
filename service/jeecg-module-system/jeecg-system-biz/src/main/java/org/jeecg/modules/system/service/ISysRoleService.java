package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * character sheet Service category
 * </p>
 *
 * @Author scott
 * @since 2018-12-19
 */
public interface ISysRoleService extends IService<SysRole> {
    /**
     * Query all roles（No tenant isolation）
     * @param page
     * @param role
     * @return
     */
    Page<SysRole> listAllSysRole(@Param("page") Page<SysRole> page, SysRole role);

    /**
     * 查询Role是否存在No tenant isolation
     *
     * @param roleCode
     * @return
     */
    SysRole getRoleNoTenant(@Param("roleCode") String roleCode);
    
    /**
     * import excel ，examine roleCode uniqueness
     *
     * @param file
     * @param params
     * @return
     * @throws Exception
     */
    Result importExcelCheckRoleCode(MultipartFile file, ImportParams params) throws Exception;

    /**
     * Delete role
     * @param roleid
     * @return
     */
    public boolean deleteRole(String roleid);

    /**
     * 批量Delete role
     * @param roleids
     * @return
     */
    public boolean deleteBatchRole(String[] roleids);

    /**
     * According to roleidand the current tenant to determine whether the current role exists in this tenant.
     * @param id
     * @return
     */
    Long getRoleCountByTenantId(String id, Integer tenantId);

    /**
     * Verify if it isadminRole
     * 
     * @param ids
     */
    void checkAdminRoleRejectDel(String ids);
}
