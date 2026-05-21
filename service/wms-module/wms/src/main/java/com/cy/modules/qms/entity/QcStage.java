package com.cy.modules.qms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Công đoạn kiểm tra chất lượng")
@TableName("qms_qc_stage")
public class QcStage extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Mã công đoạn")
    private String stageCode;

    @Schema(description = "Tên công đoạn")
    private String stageName;

    @Schema(description = "Mô tả công đoạn")
    private String description;

    @Schema(description = "Thứ tự sắp xếp")
    private Integer sortOrder;

    @Schema(description = "Trạng thái: active | inactive")
    private String status;
}
