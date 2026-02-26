package org.jeecg.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.dto.DataLogDTO;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.system.entity.SysComment;
import org.jeecg.modules.system.service.ISysCommentService;
import org.jeecg.modules.system.vo.SysCommentFileVo;
import org.jeecg.modules.system.vo.SysCommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: System comment reply form
 * @Author: jeecg-boot
 * @Date: 2022-07-19
 * @Version: V1.0
 */
@Tag(name = "System comment reply form")
@RestController
@RequestMapping("/sys/comment")
@Slf4j
public class SysCommentController extends JeecgController<SysComment, ISysCommentService> {

    @Autowired
    private ISysCommentService sysCommentService;

    @Autowired
    private ISysBaseAPI sysBaseAPI;


    /**
     * Online preview file address
     */
    @Value("${jeecg.file-view-domain}/onlinePreview")
    private String onlinePreviewDomain;

    /**
     * Query comments+document
     *
     * @param sysComment
     * @return
     */
    @Operation(summary = "System comment reply form-List query")
    @GetMapping(value = "/listByForm")
    public Result<IPage<SysCommentVO>> queryListByForm(SysComment sysComment) {
        List<SysCommentVO> list = sysCommentService.queryFormCommentInfo(sysComment);
        IPage<SysCommentVO> pageList = new Page();
        pageList.setRecords(list);
        return Result.OK(pageList);
    }

    /**
     * Querydocument
     *
     * @param sysComment
     * @return
     */
    @Operation(summary = "System comment reply form-List query")
    @GetMapping(value = "/fileList")
    public Result<IPage<SysCommentFileVo>> queryFileList(SysComment sysComment) {
        List<SysCommentFileVo> list = sysCommentService.queryFormFileList(sysComment.getTableName(), sysComment.getTableDataId());
        IPage<SysCommentFileVo> pageList = new Page();
        pageList.setRecords(list);
        return Result.OK(pageList);
    }

    @Operation(summary = "System review form-Add text")
    @PostMapping(value = "/addText")
    public Result<String> addText(@RequestBody SysComment sysComment) {
        String commentId = sysCommentService.saveOne(sysComment);
        return Result.OK(commentId);
    }

    @Operation(summary = "System review form-Add todocument")
    @PostMapping(value = "/addFile")
    public Result<String> addFile(HttpServletRequest request) {
        try {
            sysCommentService.saveOneFileComment(request);
            return Result.OK("success");
        } catch (Exception e) {
            log.error("评论document上传失败：{}", e.getMessage());
            return Result.error("Operation failed," + e.getMessage());
        }
    }

    /**
     * appAdd comment form
     * @param request
     * @return
     */
    @Operation(summary = "System review form-Add todocument")
    @PostMapping(value = "/appAddFile")
    public Result<String> appAddFile(HttpServletRequest request) {
        try {
            sysCommentService.appSaveOneFileComment(request);
            return Result.OK("success");
        } catch (Exception e) {
            log.error("评论document上传失败：{}", e.getMessage());
            return Result.error("Operation failed," + e.getMessage());
        }
    }

    @Operation(summary = "System comment reply form-passiddelete")
    @DeleteMapping(value = "/deleteOne")
    public Result<String> deleteOne(@RequestParam(name = "id", required = true) String id) {
        SysComment comment = sysCommentService.getById(id);
        if(comment==null){
            return Result.error("该评论已被delete！");
        }
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String username = sysUser.getUsername();
        String admin = "admin";
        //Apart fromadminoutside 其他人只能delete自己的评论
        if((!admin.equals(username)) && !username.equals(comment.getCreateBy())){
            return Result.error("只能delete自己的评论！");
        }
        sysCommentService.deleteOne(id);
        //delete评论Add to日志
        String logContent = "delete了评论， "+ comment.getCommentContent();
        DataLogDTO dataLog = new DataLogDTO(comment.getTableName(), comment.getTableDataId(), logContent, CommonConstant.DATA_LOG_TYPE_COMMENT);
        sysBaseAPI.saveDataLog(dataLog);
        return Result.OK("delete成功!");
    }


    /**
     * 获取document预览的地址
     * @return
     */
    @GetMapping(value = "/getFileViewDomain")
    public Result<String> getFileViewDomain() {
        return Result.OK(onlinePreviewDomain);
    }


    /**
     * 分页List query
     *
     * @param sysComment
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "System comment reply form-分页List query")
    @Operation(summary = "System comment reply form-分页List query")
    @GetMapping(value = "/list")
    public Result<IPage<SysComment>> queryPageList(SysComment sysComment,
                                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                   HttpServletRequest req) {
        QueryWrapper<SysComment> queryWrapper = QueryGenerator.initQueryWrapper(sysComment, req.getParameterMap());
        Page<SysComment> page = new Page<SysComment>(pageNo, pageSize);
        IPage<SysComment> pageList = sysCommentService.page(page, queryWrapper);
        return Result.OK(pageList);
    }


    /**
     * Add to
     *
     * @param sysComment
     * @return
     */
    @Operation(summary = "System comment reply form-Add to")
    //@RequiresPermissions("org.jeecg.modules.demo:sys_comment:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody SysComment sysComment) {
        sysCommentService.save(sysComment);
        return Result.OK("Add to成功！");
    }

    /**
     * edit
     *
     * @param sysComment
     * @return
     */
    //@AutoLog(value = "System comment reply form-edit")
    @Operation(summary = "System comment reply form-edit")
    //@RequiresPermissions("org.jeecg.modules.demo:sys_comment:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody SysComment sysComment) {
        sysCommentService.updateById(sysComment);
        return Result.OK("edit成功!");
    }

    /**
     * passiddelete
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "System comment reply form-passiddelete")
    @Operation(summary = "System comment reply form-passiddelete")
    //@RequiresPermissions("org.jeecg.modules.demo:sys_comment:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        sysCommentService.removeById(id);
        return Result.OK("delete成功!");
    }

    /**
     * 批量delete
     *
     * @param ids
     * @return
     */
    //@AutoLog(value = "System comment reply form-批量delete")
    @Operation(summary = "System comment reply form-批量delete")
    //@RequiresPermissions("org.jeecg.modules.demo:sys_comment:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.sysCommentService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量delete成功!");
    }

    /**
     * passidQuery
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "System comment reply form-passidQuery")
    @Operation(summary = "System comment reply form-passidQuery")
    @GetMapping(value = "/queryById")
    public Result<SysComment> queryById(@RequestParam(name = "id", required = true) String id) {
        SysComment sysComment = sysCommentService.getById(id);
        if (sysComment == null) {
            return Result.error("No corresponding data found");
        }
        return Result.OK(sysComment);
    }

    /**
     * Exportexcel
     *
     * @param request
     * @param sysComment
     */
    //@RequiresPermissions("org.jeecg.modules.demo:sys_comment:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SysComment sysComment) {
        return super.exportXls(request, sysComment, SysComment.class, "System comment reply form");
    }

    /**
     * passexcelImport data
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("sys_comment:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, SysComment.class);
    }


}
