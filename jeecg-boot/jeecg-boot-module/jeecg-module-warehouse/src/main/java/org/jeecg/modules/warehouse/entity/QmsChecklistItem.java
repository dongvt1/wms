package org.jeecg.modules.warehouse.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @Description: Chi tiết tiêu chí trong mẫu checklist
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Chi tiết tiêu chí trong mẫu checklist")
@TableName("wh_qms_checklist_item")
public class QmsChecklistItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    /** FK → wh_qms_checklist_template */
    @Schema(description = "ID mẫu checklist")
    private String templateId;

    /** Thứ tự */
    @Schema(description = "Thứ tự hiển thị")
    private Integer itemOrder;

    /** Tên tiêu chí */
    @Schema(description = "Tên tiêu chí kiểm tra")
    private String criterionName;

    /** Giá trị tiêu chuẩn */
    @Schema(description = "Giá trị tiêu chuẩn / yêu cầu")
    private String standardValue;

    /** Kiểu nhập liệu */
    @Schema(description = "Kiểu nhập: text, number, pass_fail, select")
    private String inputType;

    /** Options (JSON) nếu type = select */
    @Schema(description = "JSON options nếu input_type=select")
    private String options;

    /** Bắt buộc */
    @Schema(description = "1=bắt buộc, 0=không bắt buộc")
    private Integer isRequired;

    /** Ghi chú */
    @Schema(description = "Ghi chú")
    private String notes;
}
