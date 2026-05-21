package com.cy.modules.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.modules.qms.entity.IqcInspection;
import com.cy.modules.qms.entity.PqcInspection;
import com.cy.modules.qms.entity.QmsNotification;
import com.cy.modules.qms.mapper.IqcInspectionMapper;
import com.cy.modules.qms.mapper.PqcInspectionMapper;
import com.cy.modules.qms.mapper.QmsNotificationMapper;
import com.cy.modules.qms.service.QmsNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @Description: QMS Notification Service Implementation
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Service
@Slf4j
public class QmsNotificationServiceImpl extends ServiceImpl<QmsNotificationMapper, QmsNotification>
        implements QmsNotificationService {

    @Autowired
    private IqcInspectionMapper iqcInspectionMapper;

    @Autowired
    private PqcInspectionMapper pqcInspectionMapper;

    @Override
    public void sendApprovalRequest(String entityType, String entityId, String title, String targetUserId) {
        QmsNotification notification = new QmsNotification();
        notification.setId(UUID.randomUUID().toString());
        notification.setUserId(targetUserId);
        notification.setTitle(title);
        notification.setContent("Có phiếu " + entityType.toUpperCase() + " cần phê duyệt");
        notification.setEntityType(entityType);
        notification.setEntityId(entityId);
        notification.setIsRead(0);
        notification.setCreateTime(new Date());
        this.save(notification);
        log.info("Sent approval request notification to user {} for {} {}", targetUserId, entityType, entityId);
    }

    @Override
    public void sendApprovalResult(String entityType, String entityId, String result, String targetUserId) {
        QmsNotification notification = new QmsNotification();
        notification.setId(UUID.randomUUID().toString());
        notification.setUserId(targetUserId);
        notification.setTitle("Kết quả phê duyệt phiếu " + entityType.toUpperCase());
        notification.setContent("Phiếu " + entityType.toUpperCase() + " đã được phê duyệt với kết quả: " + result);
        notification.setEntityType(entityType);
        notification.setEntityId(entityId);
        notification.setIsRead(0);
        notification.setCreateTime(new Date());
        this.save(notification);
        log.info("Sent approval result notification to user {} for {} {} result={}", targetUserId, entityType, entityId, result);
    }

    @Override
    public long getUnreadCount(String userId) {
        QueryWrapper<QmsNotification> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        qw.eq("is_read", 0);
        return this.count(qw);
    }

    @Override
    public void markRead(String id) {
        UpdateWrapper<QmsNotification> uw = new UpdateWrapper<>();
        uw.eq("id", id);
        uw.set("is_read", 1);
        this.update(uw);
    }

    @Override
    public void markAllRead(String userId) {
        UpdateWrapper<QmsNotification> uw = new UpdateWrapper<>();
        uw.eq("user_id", userId);
        uw.eq("is_read", 0);
        uw.set("is_read", 1);
        this.update(uw);
    }

    @Override
    public void sendReminder() {
        Date threshold = getThreshold24h();

        // Find IQC inspections pending_approval > 24h
        sendRemindersForIqc(threshold);

        // Find PQC inspections pending_approval > 24h
        sendRemindersForPqc(threshold);

        // FQC reminders will be added when FQC module is implemented (task 7.3)
        log.info("Reminder job completed");
    }

    private void sendRemindersForIqc(Date threshold) {
        QueryWrapper<IqcInspection> qw = new QueryWrapper<>();
        qw.eq("status", "pending_approval");
        qw.le("update_time", threshold);
        List<IqcInspection> overdueList = iqcInspectionMapper.selectList(qw);

        for (IqcInspection inspection : overdueList) {
            // Check if a reminder was already sent recently (within last 24h) to avoid spam
            if (!hasRecentReminder("iqc", inspection.getId(), threshold)) {
                // Send reminder to the updater (who should be the approver) or use a default QC manager
                String targetUserId = inspection.getUpdateBy() != null ? inspection.getUpdateBy() : inspection.getCreateBy();
                QmsNotification notification = new QmsNotification();
                notification.setId(UUID.randomUUID().toString());
                notification.setUserId(targetUserId);
                notification.setTitle("Nhắc nhở: Phiếu IQC " + inspection.getInspectionCode() + " chờ phê duyệt quá 24h");
                notification.setContent("Phiếu IQC " + inspection.getInspectionCode() + " đang chờ phê duyệt quá 24 giờ. Vui lòng xử lý.");
                notification.setEntityType("iqc");
                notification.setEntityId(inspection.getId());
                notification.setIsRead(0);
                notification.setCreateTime(new Date());
                this.save(notification);
            }
        }
        log.info("Sent {} IQC reminder notifications", overdueList.size());
    }

    private void sendRemindersForPqc(Date threshold) {
        QueryWrapper<PqcInspection> qw = new QueryWrapper<>();
        qw.eq("status", "pending_approval");
        qw.le("update_time", threshold);
        List<PqcInspection> overdueList = pqcInspectionMapper.selectList(qw);

        for (PqcInspection inspection : overdueList) {
            if (!hasRecentReminder("pqc", inspection.getId(), threshold)) {
                String targetUserId = inspection.getUpdateBy() != null ? inspection.getUpdateBy() : inspection.getCreateBy();
                QmsNotification notification = new QmsNotification();
                notification.setId(UUID.randomUUID().toString());
                notification.setUserId(targetUserId);
                notification.setTitle("Nhắc nhở: Phiếu PQC " + inspection.getInspectionCode() + " chờ phê duyệt quá 24h");
                notification.setContent("Phiếu PQC " + inspection.getInspectionCode() + " đang chờ phê duyệt quá 24 giờ. Vui lòng xử lý.");
                notification.setEntityType("pqc");
                notification.setEntityId(inspection.getId());
                notification.setIsRead(0);
                notification.setCreateTime(new Date());
                this.save(notification);
            }
        }
        log.info("Sent {} PQC reminder notifications", overdueList.size());
    }

    /**
     * Check if a reminder notification was already sent for this entity within the last 24h
     */
    private boolean hasRecentReminder(String entityType, String entityId, Date threshold) {
        QueryWrapper<QmsNotification> qw = new QueryWrapper<>();
        qw.eq("entity_type", entityType);
        qw.eq("entity_id", entityId);
        qw.like("title", "Nhắc nhở%");
        qw.gt("create_time", threshold);
        return this.count(qw) > 0;
    }

    private Date getThreshold24h() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -24);
        return cal.getTime();
    }
}
