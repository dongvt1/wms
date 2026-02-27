package qms.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @Description: Kết quả từng tiêu chí kiểm tra IQC
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Kết quả từng tiêu chí kiểm tra IQC")
@TableName("qms_iqc_inspection_result")
public class IqcInspectionResult implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    @Schema(description = "ID phiếu IQC")
    private String inspectionId;

    @Schema(description = "ID tiêu chí (FK wh_qms_checklist_item)")
    private String checklistItemId;

    @Schema(description = "Tên tiêu chí (copy)")
    private String criterionName;

    @Schema(description = "Giá trị tiêu chuẩn (copy)")
    private String standardValue;

    @Schema(description = "Giá trị thực đo")
    private String actualValue;

    @Schema(description = "Kết quả: passed, failed, na")
    private String result;

    @Schema(description = "Ghi chú")
    private String notes;
}
