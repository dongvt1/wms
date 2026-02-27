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
@Schema(description = "Chi tiết nhiều lần đo (list input)")
@TableName("qms_qc_session_value_item")
public class QcSessionValueItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    @Schema(description = "FK → wh_qc_session_value")
    private String valueId;

    @Schema(description = "Số thứ tự lần đo")
    private Integer seqNo;

    @Schema(description = "Giá trị đo được lần này")
    private String measuredValue;

    @Schema(description = "Kết quả: passed | failed | na")
    private String result;

    @Schema(description = "Ghi chú")
    private String notes;
}
