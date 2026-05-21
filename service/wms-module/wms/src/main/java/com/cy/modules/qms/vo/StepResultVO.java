package com.cy.modules.qms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description: VO kết quả bước kiểm tra (output, nested trong InspectionExecutionVO)
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@Schema(description = "VO kết quả bước kiểm tra")
public class StepResultVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private String id;

    @Schema(description = "FK → qms_inspection_step (snapshot)")
    private String stepId;

    @Schema(description = "Tên bước (snapshot)")
    private String stepName;

    @Schema(description = "Thứ tự (snapshot)")
    private Integer sortOrder;

    @Schema(description = "Bắt buộc (snapshot)")
    private Integer isMandatory;

    @Schema(description = "Kết quả: pass, fail, pending")
    private String result;

    @Schema(description = "Trạng thái: pending, completed, approved, rejected, re_inspect")
    private String status;

    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian hoàn thành")
    private Date completedTime;

    @Schema(description = "Ghi chú")
    private String notes;

    @Schema(description = "Danh sách giá trị trường dữ liệu")
    private List<FieldValueVO> fields;
}
