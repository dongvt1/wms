package org.jeecg.modules.planning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.planning.entity.WorkCenter;

import java.util.List;

/**
 * @Description: Work Center Service
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface WorkCenterService extends IService<WorkCenter> {

    List<WorkCenter> getByStatus(String status);

    List<WorkCenter> getByProductionLineId(String productionLineId);

    boolean isCodeUnique(String centerCode, String excludeId);
}
