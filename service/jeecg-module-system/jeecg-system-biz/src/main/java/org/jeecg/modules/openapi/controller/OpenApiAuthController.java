package org.jeecg.modules.openapi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.openapi.entity.OpenApiAuth;
import org.jeecg.modules.openapi.generator.AKSKGenerator;
import org.jeecg.modules.openapi.service.OpenApiAuthService;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @date 2024/12/10 9:54
 */
@RestController
@RequestMapping("/openapi/auth")
public class OpenApiAuthController extends JeecgController<OpenApiAuth, OpenApiAuthService> {
    
    /**
     * Paginated list query
     *
     * @param openApiAuth
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/list")
    public Result<?> queryPageList(OpenApiAuth openApiAuth, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<OpenApiAuth> queryWrapper = QueryGenerator.initQueryWrapper(openApiAuth, req.getParameterMap());
        Page<OpenApiAuth> page = new Page<>(pageNo, pageSize);
        IPage<OpenApiAuth> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * Add to
     *
     * @param openApiAuth
     * @return
     */
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody OpenApiAuth openApiAuth) {
        service.save(openApiAuth);
        return Result.ok("Add to成功！");
    }

    /**
     * edit
     *
     * @param openApiAuth
     * @return
     */
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody OpenApiAuth openApiAuth) {
        service.updateById(openApiAuth);
        return Result.ok("Modification successful!");

    }

    /**
     * passiddelete
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        service.removeById(id);
        return Result.ok("delete成功!");
    }

    /**
     * 批量delete
     *
     * @param ids
     * @return
     */
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {

        this.service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量delete成功！");
    }

    /**
     * passidQuery
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        OpenApiAuth openApiAuth = service.getById(id);
        return Result.ok(openApiAuth);
    }

    /**
     * generateAKSK
     * @return
     */
    @GetMapping("genAKSK")
    public Result<String[]> genAKSK() {
        return Result.ok(AKSKGenerator.genAKSKPair());
    }
}
