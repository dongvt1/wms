package com.cy.modules.qms.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: Tệp đính kèm QMS
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Tệp đính kèm QMS")
@TableName("qms_attachment")
public class QmsAttachment implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "ID")
    private String id;

    /** Loại thực thể: iqc/pqc/fqc/ncr */
    @Schema(description = "Loại thực thể: iqc/pqc/fqc/ncr")
    private String entityType;

    /** ID thực thể liên kết */
    @Schema(description = "ID thực thể liên kết")
    private String entityId;

    /** Tên tệp */
    @Schema(description = "Tên tệp gốc")
    private String fileName;

    /** Đường dẫn lưu trữ */
    @Schema(description = "Đường dẫn lưu trữ tệp")
    private String filePath;

    /** Dung lượng tệp (bytes) */
    @Schema(description = "Dung lượng tệp (bytes)")
    private Long fileSize;

    /** Loại tệp: jpg/png/pdf/docx/xlsx */
    @Schema(description = "Loại tệp: jpg/png/pdf/docx/xlsx")
    private String fileType;

    /** Người tải lên */
    @Schema(description = "Người tải lên")
    private String uploadBy;

    /** Thời gian tải lên */
    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian tải lên")
    private Date uploadTime;
}
