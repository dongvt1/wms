package org.jeecg.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.system.entity.SysFormFile;
import org.jeecg.modules.system.service.ISysFormFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @Description: form comment file
 * @Author: jeecg-boot
 * @Date: 2022-07-21
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "form comment file")
@RestController
@RequestMapping("/sys/formFile")
public class SysFormFileController extends JeecgController<SysFormFile, ISysFormFileService> {
    @Autowired
    private ISysFormFileService sysFormFileService;

    /**
     * Paginated list query
     *
     * @param sysFormFile
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "form comment file-Paginated list query")
    @Operation(summary = "form comment file-Paginated list query")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(SysFormFile sysFormFile,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<SysFormFile> queryWrapper = QueryGenerator.initQueryWrapper(sysFormFile, req.getParameterMap());
        Page<SysFormFile> page = new Page<SysFormFile>(pageNo, pageSize);
        IPage<SysFormFile> pageList = sysFormFileService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Add to
     *
     * @param sysFormFile
     * @return
     */
    @AutoLog(value = "form comment file-Add to")
    @Operation(summary = "form comment file-Add to")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody SysFormFile sysFormFile) {
        sysFormFileService.save(sysFormFile);
        return Result.OK("Add to成功！");
    }

    /**
     * edit
     *
     * @param sysFormFile
     * @return
     */
    @AutoLog(value = "form comment file-edit")
    @Operation(summary = "form comment file-edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<?> edit(@RequestBody SysFormFile sysFormFile) {
        sysFormFileService.updateById(sysFormFile);
        return Result.OK("edit成功!");
    }

    /**
     * passiddelete
     *
     * @param id
     * @return
     */
    @AutoLog(value = "form comment file-passiddelete")
    @Operation(summary = "form comment file-passiddelete")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        sysFormFileService.removeById(id);
        return Result.OK("delete成功!");
    }

    /**
     * 批量delete
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "form comment file-批量delete")
    @Operation(summary = "form comment file-批量delete")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.sysFormFileService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量delete成功！");
    }

    /**
     * passidQuery
     *
     * @param id
     * @return
     */
    @AutoLog(value = "form comment file-passidQuery")
    @Operation(summary = "form comment file-passidQuery")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        SysFormFile sysFormFile = sysFormFileService.getById(id);
        return Result.OK(sysFormFile);
    }

    /**
     * Exportexcel
     *
     * @param request
     * @param sysFormFile
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SysFormFile sysFormFile) {
        return super.exportXls(request, sysFormFile, SysFormFile.class, "form comment file");
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
        return super.importExcel(request, response, SysFormFile.class);
    }

}
