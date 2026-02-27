package qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import qms.entity.QmsChecklistItem;
import qms.entity.QmsChecklistTemplate;
import qms.mapper.QmsChecklistItemMapper;
import qms.mapper.QmsChecklistTemplateMapper;
import qms.service.QmsChecklistTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Description: QMS Checklist Template Service Implementation
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Service
@Slf4j
public class QmsChecklistTemplateServiceImpl extends ServiceImpl<QmsChecklistTemplateMapper, QmsChecklistTemplate>
        implements QmsChecklistTemplateService {

    @Autowired
    private QmsChecklistItemMapper checklistItemMapper;

    @Override
    public boolean isCodeUnique(String templateCode, String excludeId) {
        QueryWrapper<QmsChecklistTemplate> qw = new QueryWrapper<>();
        qw.eq("template_code", templateCode);
        if (excludeId != null) qw.ne("id", excludeId);
        return count(qw) == 0;
    }

    @Override
    public List<QmsChecklistItem> getItems(String templateId) {
        QueryWrapper<QmsChecklistItem> qw = new QueryWrapper<>();
        qw.eq("template_id", templateId).orderByAsc("item_order");
        return checklistItemMapper.selectList(qw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithItems(QmsChecklistTemplate template, List<QmsChecklistItem> items) {
        this.save(template);
        saveItems(template.getId(), items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithItems(QmsChecklistTemplate template, List<QmsChecklistItem> items) {
        this.updateById(template);
        // Replace all items
        QueryWrapper<QmsChecklistItem> delQw = new QueryWrapper<>();
        delQw.eq("template_id", template.getId());
        checklistItemMapper.delete(delQw);
        saveItems(template.getId(), items);
    }

    @Override
    public Map<String, Object> getTemplateDetail(String templateId) {
        Map<String, Object> result = new HashMap<>();
        result.put("template", this.getById(templateId));
        result.put("items", getItems(templateId));
        return result;
    }

    @Override
    public List<QmsChecklistTemplate> getActiveByType(String inspectionType) {
        QueryWrapper<QmsChecklistTemplate> qw = new QueryWrapper<>();
        qw.eq("status", "active");
        if (inspectionType != null && !inspectionType.isEmpty()) {
            qw.eq("inspection_type", inspectionType);
        }
        qw.orderByAsc("template_code");
        return this.list(qw);
    }

    private void saveItems(String templateId, List<QmsChecklistItem> items) {
        if (items == null || items.isEmpty()) return;
        int order = 1;
        for (QmsChecklistItem item : items) {
            item.setId(UUID.randomUUID().toString());
            item.setTemplateId(templateId);
            item.setItemOrder(order++);
            if (item.getInputType() == null) item.setInputType("pass_fail");
            if (item.getIsRequired() == null) item.setIsRequired(1);
            checklistItemMapper.insert(item);
        }
    }
}
