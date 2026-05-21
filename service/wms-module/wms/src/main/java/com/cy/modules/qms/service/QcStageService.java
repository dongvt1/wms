package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.modules.qms.entity.QcStage;
import com.cy.modules.qms.entity.QcStageParam;

import java.util.List;
import java.util.Map;

public interface QcStageService extends IService<QcStage> {

    String generateStageCode();

    void saveWithParams(QcStage stage, List<QcStageParam> params);

    void updateWithParams(QcStage stage, List<QcStageParam> params);

    List<QcStageParam> getParams(String stageId);

    Map<String, Object> getDetail(String stageId);

    List<QcStage> listActive();
}
