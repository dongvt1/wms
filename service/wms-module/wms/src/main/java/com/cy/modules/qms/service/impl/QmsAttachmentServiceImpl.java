package com.cy.modules.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.modules.qms.entity.QmsAttachment;
import com.cy.modules.qms.mapper.QmsAttachmentMapper;
import com.cy.modules.qms.service.QmsAttachmentService;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.CommonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * @Description: QMS Attachment Service Implementation
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Service
@Slf4j
public class QmsAttachmentServiceImpl extends ServiceImpl<QmsAttachmentMapper, QmsAttachment>
        implements QmsAttachmentService {

    /** Định dạng tệp được phép */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "pdf", "docx", "xlsx");

    /** Dung lượng tối đa: 10MB */
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    /** Số tệp đính kèm tối đa cho mỗi thực thể */
    private static final int MAX_ATTACHMENTS_PER_ENTITY = 10;

    /** Thư mục lưu trữ tệp QMS */
    private static final String BIZ_PATH = "qms";

    @Value(value = "${jeecg.uploadType:local}")
    private String uploadType;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QmsAttachment upload(MultipartFile file, String entityType, String entityId) {
        // 1. Kiểm tra định dạng và dung lượng
        validateFile(file);

        // 2. Kiểm tra số lượng tệp đính kèm
        long currentCount = countByEntity(entityType, entityId);
        if (currentCount >= MAX_ATTACHMENTS_PER_ENTITY) {
            throw new JeecgBootException("Đã đạt giới hạn 10 tệp đính kèm");
        }

        // 3. Lưu tệp qua JeecgBoot upload mechanism
        String filePath;
        try {
            filePath = CommonUtils.upload(file, BIZ_PATH, uploadType);
        } catch (Exception e) {
            log.error("Lỗi khi tải tệp lên: {}", e.getMessage(), e);
            throw new JeecgBootException("Lỗi khi tải tệp lên: " + e.getMessage());
        }

        if (filePath == null || filePath.isEmpty()) {
            throw new JeecgBootException("Tải tệp lên thất bại");
        }

        // 4. Lưu thông tin tệp đính kèm vào database
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        QmsAttachment attachment = new QmsAttachment();
        attachment.setEntityType(entityType);
        attachment.setEntityId(entityId);
        attachment.setFileName(originalFilename);
        attachment.setFilePath(filePath);
        attachment.setFileSize(file.getSize());
        attachment.setFileType(extension.toLowerCase());
        attachment.setUploadTime(new Date());

        this.save(attachment);
        return attachment;
    }

    @Override
    public List<QmsAttachment> listByEntity(String entityType, String entityId) {
        QueryWrapper<QmsAttachment> qw = new QueryWrapper<>();
        qw.eq("entity_type", entityType)
          .eq("entity_id", entityId)
          .orderByDesc("upload_time");
        return this.list(qw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachment(String attachmentId) {
        QmsAttachment attachment = this.getById(attachmentId);
        if (attachment == null) {
            throw new JeecgBootException("Không tìm thấy tệp đính kèm");
        }
        this.removeById(attachmentId);
    }

    @Override
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new JeecgBootException("Tệp không được để trống");
        }

        // Kiểm tra định dạng
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new JeecgBootException(
                "Định dạng tệp không được hỗ trợ. Chấp nhận: JPG, PNG, PDF, DOCX, XLSX");
        }

        // Kiểm tra dung lượng
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new JeecgBootException("Dung lượng tệp vượt quá 10MB");
        }
    }

    @Override
    public long countByEntity(String entityType, String entityId) {
        QueryWrapper<QmsAttachment> qw = new QueryWrapper<>();
        qw.eq("entity_type", entityType)
          .eq("entity_id", entityId);
        return this.count(qw);
    }

    /**
     * Lấy phần mở rộng của tệp từ tên tệp.
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
