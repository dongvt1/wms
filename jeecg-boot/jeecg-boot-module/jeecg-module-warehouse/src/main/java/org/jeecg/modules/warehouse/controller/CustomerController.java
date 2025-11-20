package org.jeecg.modules.warehouse.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.warehouse.entity.Customer;
import org.jeecg.modules.warehouse.service.ICustomerService;
import org.jeecg.modules.warehouse.service.ICustomerBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: Customer Controller
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Customer Management")
@RestController
@RequestMapping("/warehouse/customer")
public class CustomerController extends JeecgController<Customer, ICustomerService> {

    @Autowired
    private ICustomerService customerService;

    @Autowired
    private ICustomerBalanceService customerBalanceService;

    /**
     * Paginated list query
     *
     * @param customer
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @Operation(summary = "Get Customer data list")
    @GetMapping(value = "/list")
    @PermissionData(pageComponent = "warehouse/customer/list")
    public Result<?> list(Customer customer, 
                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, 
                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                       HttpServletRequest req) {
        QueryWrapper<Customer> queryWrapper = QueryGenerator.initQueryWrapper(customer, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<Customer> page = new Page<Customer>(pageNo, pageSize);

        IPage<Customer> pageList = customerService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Add
     *
     * @param customer
     * @return
     */
    @PostMapping(value = "/add")
    @AutoLog(value = "Add Customer")
    @Operation(summary = "Add Customer")
    public Result<?> add(@RequestBody Customer customer) {
        // Check if customer code is unique
        if (!customerService.isCodeUnique(customer.getCustomerCode(), null)) {
            return Result.error("Customer code already exists!");
        }
        
        customerService.save(customer);
        return Result.OK("Add successful!");
    }

    /**
     * Edit
     *
     * @param customer
     * @return
     */
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    @AutoLog(value = "Edit Customer", operateType = 3)
    @Operation(summary = "Edit Customer")
    public Result<?> edit(@RequestBody Customer customer) {
        // Check if customer code is unique (excluding current customer)
        if (!customerService.isCodeUnique(customer.getCustomerCode(), customer.getId())) {
            return Result.error("Customer code already exists!");
        }
        
        customerService.updateById(customer);
        return Result.OK("Update successful!");
    }

    /**
     * Delete by id
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Delete Customer")
    @DeleteMapping(value = "/delete")
    @Operation(summary = "Delete Customer by ID")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        customerService.removeById(id);
        return Result.OK("Delete successful!");
    }

    /**
     * Batch delete
     *
     * @param ids
     * @return
     */
    @DeleteMapping(value = "/deleteBatch")
    @Operation(summary = "Batch delete Customer")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        this.customerService.removeByIds(idList);
        return Result.OK("Batch delete successful!");
    }

    /**
     * Query by id
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    @Operation(summary = "Query Customer by ID")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        Customer customer = customerService.getById(id);
        return Result.OK(customer);
    }

    /**
     * Get customer by code
     *
     * @param customerCode
     * @return
     */
    @GetMapping(value = "/getByCode")
    @Operation(summary = "Get Customer by Code")
    public Result<?> getByCode(@RequestParam(name = "customerCode", required = true) String customerCode) {
        Customer customer = customerService.getByCode(customerCode);
        return Result.OK(customer);
    }

    /**
     * Get active customers
     *
     * @return
     */
    @GetMapping(value = "/getActive")
    @Operation(summary = "Get Active Customers")
    public Result<?> getActiveCustomers() {
        List<Customer> list = customerService.getActiveCustomers();
        return Result.OK(list);
    }

    /**
     * Search customers
     *
     * @param keyword
     * @return
     */
    @GetMapping(value = "/search")
    @Operation(summary = "Search Customers")
    public Result<?> searchCustomers(@RequestParam(name = "keyword", required = true) String keyword) {
        List<Customer> list = customerService.searchCustomers(keyword);
        return Result.OK(list);
    }

    /**
     * Get customer order history
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/orderHistory")
    @Operation(summary = "Get Customer Order History")
    public Result<?> getCustomerOrderHistory(@RequestParam(name = "id", required = true) String id) {
        List<Object> orderHistory = customerService.getCustomerOrderHistory(id);
        return Result.OK(orderHistory);
    }

    /**
     * Get customer balance
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/balance")
    @Operation(summary = "Get Customer Balance")
    public Result<?> getCustomerBalance(@RequestParam(name = "id", required = true) String id) {
        var balance = customerBalanceService.getByCustomerId(id);
        return Result.OK(balance);
    }

    /**
     * Update customer balance
     *
     * @param id
     * @param amount
     * @return
     */
    @PostMapping(value = "/updateBalance")
    @AutoLog(value = "Update Customer Balance")
    @Operation(summary = "Update Customer Balance")
    public Result<?> updateCustomerBalance(
            @RequestParam(name = "id", required = true) String id,
            @RequestParam(name = "amount", required = true) BigDecimal amount) {
        boolean success = customerBalanceService.updateBalance(id, amount, "admin");
        if (success) {
            return Result.OK("Balance updated successfully");
        } else {
            return Result.error("Failed to update balance");
        }
    }

    /**
     * Get customer statistics
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/statistics")
    @Operation(summary = "Get Customer Statistics")
    public Result<?> getCustomerStatistics(@RequestParam(name = "id", required = true) String id) {
        Object statistics = customerService.getCustomerStatistics(id);
        return Result.OK(statistics);
    }

    /**
     * Export excel
     *
     * @param request
     */
    @RequestMapping(value = "/exportXls")
    @PermissionData(pageComponent = "warehouse/customer/list")
    public ModelAndView exportXls(HttpServletRequest request, Customer customer) {
        return super.exportXls(request, customer, Customer.class, "Customer");
    }

    /**
     * Import data through excel
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, Customer.class);
    }
}