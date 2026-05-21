package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.modules.qms.entity.QmsAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Description: QMS Attachment Service
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface QmsAttachmentService extends IService<QmsAttachment> {

    /**
     * Tải lên tệp đính kèm cho một thực thể QMS.
     * Thực hiện kiểm tra: định dạng, dung lượng, số lượng tối đa.
     *
     * @param file       tệp tải lên
     * @param entityType loại thực thể (iqc/pqc/fqc/ncr)
     * @param entityId   ID thực thể
     * @return đối tượng QmsAttachment đã lưu
     */
    QmsAttachment upload(MultipartFile file, String entityType, String entityId);

    /**
     * Lấy danh sách tệp đính kèm theo thực thể.
     *
     * @param entityType loại thực thể
     * @param entityId   ID thực thể
     * @return danh sách tệp đính kèm
     */
    List<QmsAttachment> listByEntity(String entityType, String entityId);

    /**
     * Xóa tệp đính kèm theo ID.
     *
     * @param attachmentId ID tệp đính kèm
     */
    void deleteAttachment(String attachmentId);

    /**
     * Kiểm tra tệp hợp lệ: định dạng (JPG/PNG/PDF/DOCX/XLSX) và dung lượng (≤10MB).
     * Ném exception nếu không hợp lệ.
     *
     * @param file tệp cần kiểm tra
     */
    void validateFile(MultipartFile file);

    /**
     * Đếm số tệp đính kèm của một thực thể.
     *
     * @param entityType loại thực thể
     * @param entityId   ID thực thể
     * @return số lượng tệp đính kèm
     */
    long countByEntity(String entityType, String entityId);
}
