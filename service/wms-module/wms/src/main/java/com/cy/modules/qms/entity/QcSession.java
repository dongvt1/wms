package qms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Phiên kiểm tra công đoạn")
@TableName("qms_qc_session")
public class QcSession extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Mã phiên kiểm tra (SKyyyyMMddNNN)")
    private String sessionCode;

    @Schema(description = "FK → wh_work_order")
    private String workOrderId;

    @Schema(description = "FK → wh_qc_stage")
    private String stageId;

    @Schema(description = "Tên công đoạn (snapshot)")
    private String stageName;

    @Schema(description = "Người kiểm tra")
    private String inspector;

    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày kiểm tra")
    private Date inspectionDate;

    @Schema(description = "Trạng thái: draft | completed")
    private String status;

    @Schema(description = "Ghi chú")
    private String notes;
}
