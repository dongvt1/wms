package org.jeecg.modules.qms.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: Mẫu bộ tiêu chí kiểm tra chất lượng
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Mẫu bộ tiêu chí kiểm tra chất lượng")
@TableName("wh_qms_checklist_template")
public class QmsChecklistTemplate extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã mẫu */
    @Excel(name = "Mã mẫu", width = 20)
    @Schema(description = "Mã mẫu checklist")
    private String templateCode;

    /** Tên mẫu */
    @Excel(name = "Tên mẫu", width = 30)
    @Schema(description = "Tên mẫu checklist")
    private String templateName;

    /** Loại kiểm tra */
    @Excel(name = "Loại kiểm tra", width = 15)
    @Schema(description = "Loại kiểm tra: iqc, pqc")
    private String inspectionType;

    /** ID sản phẩm áp dụng (NULL = dùng chung) */
    @Excel(name = "Sản phẩm", width = 25, dictTable = "product", dicText = "name", dicCode = "id")
    @Schema(description = "ID sản phẩm áp dụng (NULL = dùng chung)")
    private String productId;

    /** Trạng thái */
    @Excel(name = "Trạng thái", width = 15)
    @Schema(description = "Trạng thái: active, inactive")
    private String status;

    /** Ghi chú */
    @Excel(name = "Ghi chú", width = 40)
    @Schema(description = "Ghi chú")
    private String notes;
}
