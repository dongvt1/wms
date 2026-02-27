package qms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Giá trị tham số trong phiên kiểm tra")
@TableName("qms_qc_session_value")
public class QcSessionValue implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    @Schema(description = "FK → wh_qc_session")
    private String sessionId;

    @Schema(description = "FK → wh_qc_stage_param")
    private String paramId;

    @Schema(description = "Tên tham số (snapshot)")
    private String paramName;

    @Schema(description = "Kiểu nhập (snapshot)")
    private String inputType;

    @Schema(description = "Đơn vị (snapshot)")
    private String unit;

    @Schema(description = "Giá trị thực (null nếu type=list)")
    private String actualValue;

    @Schema(description = "Kết quả: passed | failed | na")
    private String result;

    @Schema(description = "Thứ tự")
    private Integer sortOrder;

    @Schema(description = "Ghi chú")
    private String notes;
}
