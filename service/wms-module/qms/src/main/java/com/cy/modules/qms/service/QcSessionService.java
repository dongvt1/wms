package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.modules.qms.entity.QcSession;

import java.util.List;
import java.util.Map;

public interface QcSessionService extends IService<QcSession> {

    String generateSessionCode();

    /** Tạo session từ stage: auto-clone params thành values */
    void saveWithValues(QcSession session, List<Map<String, Object>> values);

    void updateWithValues(QcSession session, List<Map<String, Object>> values);

    /** Hoàn thành session: draft → completed */
    String completeSession(String id);

    List<Map<String, Object>> getValues(String sessionId);

    Map<String, Object> getDetail(String sessionId);

    List<QcSession> listByWorkOrder(String workOrderId);
}
