package com.cy.modules.qms.job;

import com.cy.modules.qms.service.QmsNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Description: Scheduled job gửi thông báo nhắc nhở cho phiếu pending_approval > 24h
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Slf4j
@Component
@EnableScheduling
public class QmsNotificationScheduler {

    @Autowired
    private QmsNotificationService notificationService;

    /**
     * Chạy mỗi giờ: kiểm tra phiếu pending_approval > 24h và gửi nhắc nhở
     * Cron: giây=0, phút=0, giờ=mỗi giờ, ngày=*, tháng=*, thứ=?
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void sendOverdueApprovalReminders() {
        log.info("QMS Notification Scheduler: Starting overdue approval reminder check...");
        try {
            notificationService.sendReminder();
            log.info("QMS Notification Scheduler: Completed successfully.");
        } catch (Exception e) {
            log.error("QMS Notification Scheduler: Error sending reminders", e);
        }
    }
}
