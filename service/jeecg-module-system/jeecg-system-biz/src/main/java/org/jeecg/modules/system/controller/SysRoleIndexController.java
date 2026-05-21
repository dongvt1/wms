package org.jeecg.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.constant.DefIndexConst;
import org.jeecg.modules.system.entity.SysRoleIndex;
import org.jeecg.modules.system.service.ISysRoleIndexService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;

/**
 * @Description: Role homepage configuration
 * @Author: jeecg-boot
 * @Date: 2022-03-25
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Role homepage configuration")
@RestController
@RequestMapping("/sys/sysRoleIndex")
public class SysRoleIndexController extends JeecgController<SysRoleIndex, ISysRoleIndexService> {
    @Autowired
    private ISysRoleIndexService sysRoleIndexService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private BaseCommonService baseCommonService;
    /**
     * Paginated list query
     *
     * @param sysRoleIndex
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "Role homepage configuration-Paginated list query")
    @Operation(summary = "Role homepage configuration-Paginated list query")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(SysRoleIndex sysRoleIndex,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<SysRoleIndex> queryWrapper = QueryGenerator.initQueryWrapper(sysRoleIndex, req.getParameterMap());
        Page<SysRoleIndex> page = new Page<SysRoleIndex>(pageNo, pageSize);
        IPage<SysRoleIndex> pageList = sysRoleIndexService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Add to
     *
     * @param sysRoleIndex
     * @return
     */
    @RequiresPermissions("system:roleindex:add")
    @AutoLog(value = "Role homepage configuration-Add to")
    @Operation(summary = "Role homepage configuration-Add to")
    @PostMapping(value = "/add")
    //@DynamicTable(value = DynamicTableConstant.SYS_ROLE_INDEX)
    public Result<?> add(@RequestBody SysRoleIndex sysRoleIndex,HttpServletRequest request) {
        String relationType = sysRoleIndex.getRelationType();
        if(oConvertUtils.isEmpty(relationType)){
            sysRoleIndex.setRelationType(CommonConstant.HOME_RELATION_ROLE);
        }
        sysRoleIndexService.save(sysRoleIndex);
        sysRoleIndexService.cleanDefaultIndexCache();
        return Result.OK("Add to成功！");
    }

    /**
     * edit
     *
     * @param sysRoleIndex
     * @return
     */
    @RequiresPermissions("system:roleindex:edit")
    @AutoLog(value = "Role homepage configuration-edit")
    @Operation(summary = "Role homepage configuration-edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    //@DynamicTable(value = DynamicTableConstant.SYS_ROLE_INDEX)
    public Result<?> edit(@RequestBody SysRoleIndex sysRoleIndex,HttpServletRequest request) {
        String relationType = sysRoleIndex.getRelationType();
        if(oConvertUtils.isEmpty(relationType)){
            sysRoleIndex.setRelationType(CommonConstant.HOME_RELATION_ROLE);
        }
        sysRoleIndexService.updateById(sysRoleIndex);
        sysRoleIndexService.cleanDefaultIndexCache();
        return Result.OK("edit成功!");
    }

    /**
     * passiddelete
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Role homepage configuration-passiddelete")
    @Operation(summary = "Role homepage configuration-passiddelete")
    @RequiresPermissions("system:roleindex:delete")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        sysRoleIndexService.removeById(id);
        return Result.OK("delete成功!");
    }

    /**
     * 批量delete
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "Role homepage configuration-批量delete")
    @Operation(summary = "Role homepage configuration-批量delete")
    @RequiresPermissions("system:roleindex:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        baseCommonService.addLog("批量delete用户， ids： " +ids ,CommonConstant.LOG_TYPE_2, 3);
        this.sysRoleIndexService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量delete成功！");
    }

    /**
     * passidQuery
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Role homepage configuration-passidQuery")
    @Operation(summary = "Role homepage configuration-passidQuery")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        SysRoleIndex sysRoleIndex = sysRoleIndexService.getById(id);
        return Result.OK(sysRoleIndex);
    }

    /**
     * Exportexcel
     *
     * @param request
     * @param sysRoleIndex
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SysRoleIndex sysRoleIndex) {
        return super.exportXls(request, sysRoleIndex, SysRoleIndex.class, "Role homepage configuration");
    }

    /**
     * passexcelImport data
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, SysRoleIndex.class);
    }

    /**
     * passcodeQuery
     *
     * @param roleCode
     * @return
     */
    @AutoLog(value = "Role homepage configuration-passcodeQuery")
    @Operation(summary = "Role homepage configuration-passcodeQuery")
    @GetMapping(value = "/queryByCode")
    //@DynamicTable(value = DynamicTableConstant.SYS_ROLE_INDEX)
    public Result<?> queryByCode(@RequestParam(name = "roleCode", required = true) String roleCode,HttpServletRequest request) {
        SysRoleIndex sysRoleIndex = sysRoleIndexService.getOne(new LambdaQueryWrapper<SysRoleIndex>().eq(SysRoleIndex::getRoleCode, roleCode));
        return Result.OK(sysRoleIndex);
    }

    /**
     * Query默认首页配置
     */
    @GetMapping("/queryDefIndex")
    public Result<SysRoleIndex> queryDefIndex() {
        SysRoleIndex defIndexCfg = sysRoleIndexService.queryDefaultIndex();
        return Result.OK(defIndexCfg);
    }

    /**
     * Update default homepage configuration
     */
    @RequiresPermissions("system:permission:setDefIndex")
    @PutMapping("/updateDefIndex")
    public Result<?> updateDefIndex(
            @RequestParam("url") String url,
            @RequestParam("component") String component,
            @RequestParam("isRoute") Boolean isRoute
    ) {
        boolean success = sysRoleIndexService.updateDefaultIndex(url, component, isRoute);
        if (success) {
            return Result.OK("Setup successful");
        } else {
            return Result.error("Setup failed");
        }
    }
    /**
     * Switch default portal
     *
     * @param sysRoleIndex
     * @return
     */
    @PostMapping(value = "/changeDefHome")
    public Result<?> changeDefHome(@RequestBody SysRoleIndex sysRoleIndex,HttpServletRequest request) {
        String username = JwtUtil.getUserNameByToken(request);
        sysRoleIndex.setRoleCode(username);
        sysRoleIndexService.changeDefHome(sysRoleIndex);
        //update-begin-author:liusq---date:2025-07-03--for: After the switch is completedhomePathGet
        String version = request.getHeader(CommonConstant.VERSION);
        String homePath = null;
        SysRoleIndex defIndexCfg = sysUserService.getDynamicIndexByUserRole(username, version);
        if (defIndexCfg == null) {
            defIndexCfg = sysRoleIndexService.initDefaultIndex();
        }
        if (oConvertUtils.isNotEmpty(version) && defIndexCfg != null && oConvertUtils.isNotEmpty(defIndexCfg.getUrl())) {
            homePath = defIndexCfg.getUrl();
            if (!homePath.startsWith(SymbolConstant.SINGLE_SLASH)) {
                homePath = SymbolConstant.SINGLE_SLASH + homePath;
            }
        }
        //update-end-author:liusq---date:2025-07-03--for:After the switch is completedhomePathGet
        return Result.OK(homePath);
    }
    /**
     * Get门户类型
     *
     * @return
     */
    @GetMapping(value = "/getCurrentHome")
    public Result<?> getCurrentHome(HttpServletRequest request) {
        String username = JwtUtil.getUserNameByToken(request);
        Object homeType = redisUtil.get(DefIndexConst.CACHE_TYPE + username);
        return Result.OK(oConvertUtils.getString(homeType,DefIndexConst.HOME_TYPE_SYSTEM));
    }

    /**
     * clear cache
     *
     * @return
     */
    @RequestMapping(value = "/cleanDefaultIndexCache")
    public Result<?> cleanDefaultIndexCache(HttpServletRequest request) {
        sysRoleIndexService.cleanDefaultIndexCache();
        return Result.OK();
    }
}
