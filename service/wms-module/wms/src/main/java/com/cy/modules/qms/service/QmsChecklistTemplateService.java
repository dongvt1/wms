package qms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import qms.entity.QmsChecklistItem;
import qms.entity.QmsChecklistTemplate;

import java.util.List;
import java.util.Map;

/**
 * @Description: QMS Checklist Template Service
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface QmsChecklistTemplateService extends IService<QmsChecklistTemplate> {

    boolean isCodeUnique(String templateCode, String excludeId);

    List<QmsChecklistItem> getItems(String templateId);

    void saveWithItems(QmsChecklistTemplate template, List<QmsChecklistItem> items);

    void updateWithItems(QmsChecklistTemplate template, List<QmsChecklistItem> items);

    Map<String, Object> getTemplateDetail(String templateId);

    List<QmsChecklistTemplate> getActiveByType(String inspectionType);
}
