package com.cy.modules.qms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import com.cy.modules.qms.entity.QmsNotification;
import com.cy.modules.qms.service.QmsNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: QMS Notification Controller
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Slf4j
@Tag(name = "QMS - Thông báo (Notification)")
@RestController
@RequestMapping("/qms/notification")
public class QmsNotificationController {

    @Autowired
    private QmsNotificationService notificationService;

    /**
     * Lấy danh sách thông báo của user hiện tại (phân trang)
     */
    @Operation(summary = "Danh sách thông báo của user hiện tại")
    @GetMapping("/list")
    public Result<?> list(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        LoginUser currentUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = currentUser.getId();

        QueryWrapper<QmsNotification> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        qw.orderByDesc("create_time");

        Page<QmsNotification> page = new Page<>(pageNo, pageSize);
        IPage<QmsNotification> pageList = notificationService.page(page, qw);
        return Result.OK(pageList);
    }

    /**
     * Lấy số lượng thông báo chưa đọc cho badge hiển thị
     */
    @Operation(summary = "Số lượng thông báo chưa đọc")
    @GetMapping("/unreadCount")
    public Result<?> unreadCount() {
        LoginUser currentUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = currentUser.getId();
        long count = notificationService.getUnreadCount(userId);
        return Result.OK(count);
    }

    /**
     * Đánh dấu một thông báo là đã đọc
     */
    @AutoLog(value = "Đánh dấu thông báo đã đọc")
    @Operation(summary = "Đánh dấu một thông báo là đã đọc")
    @PutMapping("/markRead/{id}")
    public Result<?> markRead(@PathVariable("id") String id) {
        notificationService.markRead(id);
        return Result.OK("Đã đánh dấu đã đọc");
    }

    /**
     * Đánh dấu tất cả thông báo của user hiện tại là đã đọc
     */
    @AutoLog(value = "Đánh dấu tất cả thông báo đã đọc")
    @Operation(summary = "Đánh dấu tất cả thông báo là đã đọc")
    @PutMapping("/markAllRead")
    public Result<?> markAllRead() {
        LoginUser currentUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = currentUser.getId();
        notificationService.markAllRead(userId);
        return Result.OK("Đã đánh dấu tất cả đã đọc");
    }
}
