package org.jeecg.modules.demo.test.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.demo.test.entity.JeecgOrderCustomer;
import org.jeecg.modules.demo.test.entity.JeecgOrderMain;
import org.jeecg.modules.demo.test.entity.JeecgOrderTicket;
import org.jeecg.modules.demo.test.service.IJeecgOrderCustomerService;
import org.jeecg.modules.demo.test.service.IJeecgOrderMainService;
import org.jeecg.modules.demo.test.service.IJeecgOrderTicketService;
import org.jeecg.modules.demo.test.vo.JeecgOrderMainPage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @Description: One-to-many example（ERP TABstyle）
 * @Author: ZhiLin
 * @Date: 2019-02-20
 * @Version: v2.0
 */
@Slf4j
@RestController
@RequestMapping("/test/order")
public class JeecgOrderErpMainController {

    @Autowired
    private IJeecgOrderMainService jeecgOrderMainService;
    @Autowired
    private IJeecgOrderCustomerService jeecgOrderCustomerService;
    @Autowired
    private IJeecgOrderTicketService jeecgOrderTicketService;

    /**
     * Paginated list query
     *
     * @param jeecgOrderMain
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/orderList")
    public Result<?> respondePagedData(JeecgOrderMain jeecgOrderMain,
                                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                       HttpServletRequest req) {
        QueryWrapper<JeecgOrderMain> queryWrapper = QueryGenerator.initQueryWrapper(jeecgOrderMain, req.getParameterMap());
        Page<JeecgOrderMain> page = new Page<JeecgOrderMain>(pageNo, pageSize);
        IPage<JeecgOrderMain> pageList = jeecgOrderMainService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * Add to
     *
     * @param jeecgOrderMainPage
     * @return
     */
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody JeecgOrderMainPage jeecgOrderMainPage) {
        JeecgOrderMain jeecgOrderMain = new JeecgOrderMain();
        BeanUtils.copyProperties(jeecgOrderMainPage, jeecgOrderMain);
        jeecgOrderMainService.save(jeecgOrderMain);
        return Result.ok("Add to成功!");
    }

    /**
     * edit
     *
     * @param jeecgOrderMainPage
     * @return
     */
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<?> edit(@RequestBody JeecgOrderMainPage jeecgOrderMainPage) {
        JeecgOrderMain jeecgOrderMain = new JeecgOrderMain();
        BeanUtils.copyProperties(jeecgOrderMainPage, jeecgOrderMain);
        jeecgOrderMainService.updateById(jeecgOrderMain);
        return Result.ok("edit成功!");
    }

    /**
     * passiddelete
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        jeecgOrderMainService.delMain(id);
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
        this.jeecgOrderMainService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量delete成功!");
    }

    /**
     * passidQuery
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        JeecgOrderMain jeecgOrderMain = jeecgOrderMainService.getById(id);
        return Result.ok(jeecgOrderMain);
    }


    /**
     * passidQuery
     *
     * @param jeecgOrderCustomer
     * @return
     */
    @GetMapping(value = "/listOrderCustomerByMainId")
    public Result<?> queryOrderCustomerListByMainId(JeecgOrderCustomer jeecgOrderCustomer,
                                                    @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                    @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                    HttpServletRequest req) {
        QueryWrapper<JeecgOrderCustomer> queryWrapper = QueryGenerator.initQueryWrapper(jeecgOrderCustomer, req.getParameterMap());
        Page<JeecgOrderCustomer> page = new Page<JeecgOrderCustomer>(pageNo, pageSize);
        IPage<JeecgOrderCustomer> pageList = jeecgOrderCustomerService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * passidQuery
     *
     * @param jeecgOrderTicket
     * @return
     */
    @GetMapping(value = "/listOrderTicketByMainId")
    public Result<?> queryOrderTicketListByMainId(JeecgOrderTicket jeecgOrderTicket,
                                                  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                  HttpServletRequest req) {
        QueryWrapper<JeecgOrderTicket> queryWrapper = QueryGenerator.initQueryWrapper(jeecgOrderTicket, req.getParameterMap());
        Page<JeecgOrderTicket> page = new Page<JeecgOrderTicket>(pageNo, pageSize);
        IPage<JeecgOrderTicket> pageList = jeecgOrderTicketService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * Add to
     *
     * @param jeecgOrderCustomer
     * @return
     */
    @PostMapping(value = "/addCustomer")
    public Result<?> addCustomer(@RequestBody JeecgOrderCustomer jeecgOrderCustomer) {
        jeecgOrderCustomerService.save(jeecgOrderCustomer);
        return Result.ok("Add to成功!");
    }

    /**
     * edit
     *
     * @param jeecgOrderCustomer
     * @return
     */
    @RequestMapping(value = "/editCustomer", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<?> editCustomer(@RequestBody JeecgOrderCustomer jeecgOrderCustomer) {
        jeecgOrderCustomerService.updateById(jeecgOrderCustomer);
        return Result.ok("Add to成功!");
    }

    /**
     * passiddelete
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/deleteCustomer")
    public Result<?> deleteCustomer(@RequestParam(name = "id", required = true) String id) {
        jeecgOrderCustomerService.removeById(id);
        return Result.ok("delete成功!");
    }

    /**
     * 批量delete
     *
     * @param ids
     * @return
     */
    @DeleteMapping(value = "/deleteBatchCustomer")
    public Result<?> deleteBatchCustomer(@RequestParam(name = "ids", required = true) String ids) {
        this.jeecgOrderCustomerService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量delete成功!");
    }

    /**
     * Add to
     *
     * @param jeecgOrderTicket
     * @return
     */
    @PostMapping(value = "/addTicket")
    public Result<?> addTicket(@RequestBody JeecgOrderTicket jeecgOrderTicket) {
        jeecgOrderTicketService.save(jeecgOrderTicket);
        return Result.ok("Add to成功!");
    }

    /**
     * edit
     *
     * @param jeecgOrderTicket
     * @return
     */
    @RequestMapping(value = "/editTicket", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<?> editTicket(@RequestBody JeecgOrderTicket jeecgOrderTicket) {
        jeecgOrderTicketService.updateById(jeecgOrderTicket);
        return Result.ok("edit成功!");
    }

    /**
     * passiddelete
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/deleteTicket")
    public Result<?> deleteTicket(@RequestParam(name = "id", required = true) String id) {
        jeecgOrderTicketService.removeById(id);
        return Result.ok("delete成功!");
    }

    /**
     * 批量delete
     *
     * @param ids
     * @return
     */
    @DeleteMapping(value = "/deleteBatchTicket")
    public Result<?> deleteBatchTicket(@RequestParam(name = "ids", required = true) String ids) {
        this.jeecgOrderTicketService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量delete成功!");
    }

}