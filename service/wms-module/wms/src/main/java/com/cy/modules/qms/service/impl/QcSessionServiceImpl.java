package com.cy.modules.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.modules.qms.entity.QcSession;
import com.cy.modules.qms.entity.QcSessionValue;
import com.cy.modules.qms.entity.QcSessionValueItem;
import com.cy.modules.qms.mapper.QcSessionMapper;
import com.cy.modules.qms.mapper.QcSessionValueMapper;
import com.cy.modules.qms.mapper.QcSessionValueItemMapper;
import com.cy.modules.qms.service.QcSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class QcSessionServiceImpl extends ServiceImpl<QcSessionMapper, QcSession>
        implements QcSessionService {

    @Autowired private QcSessionValueMapper valueMapper;
    @Autowired private QcSessionValueItemMapper itemMapper;

    @Override
    public String generateSessionCode() {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        QueryWrapper<QcSession> qw = new QueryWrapper<>();
        qw.likeRight("session_code", "SK" + dateStr).orderByDesc("session_code").last("LIMIT 1");
        QcSession last = this.getOne(qw);
        int seq = 1;
        if (last != null) {
            try { seq = Integer.parseInt(last.getSessionCode().substring(last.getSessionCode().length() - 3)) + 1; }
            catch (NumberFormatException e) { seq = 1; }
        }
        return "SK" + dateStr + String.format("%03d", seq);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithValues(QcSession session, List<Map<String, Object>> values) {
        this.save(session);
        saveValues(session.getId(), values);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithValues(QcSession session, List<Map<String, Object>> values) {
        this.updateById(session);
        // Delete existing values + items
        List<QcSessionValue> existing = valueMapper.selectList(
            new QueryWrapper<QcSessionValue>().eq("session_id", session.getId()));
        for (QcSessionValue v : existing) {
            itemMapper.delete(new QueryWrapper<QcSessionValueItem>().eq("value_id", v.getId()));
        }
        valueMapper.delete(new QueryWrapper<QcSessionValue>().eq("session_id", session.getId()));
        saveValues(session.getId(), values);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String completeSession(String id) {
        QcSession session = this.getById(id);
        if (session == null) return "Không tìm thấy phiên kiểm tra";
        session.setStatus("completed");
        this.updateById(session);
        return "Hoàn thành phiên kiểm tra thành công";
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getValues(String sessionId) {
        List<QcSessionValue> values = valueMapper.selectList(
            new QueryWrapper<QcSessionValue>().eq("session_id", sessionId).orderByAsc("sort_order"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (QcSessionValue v : values) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", v.getId());
            row.put("paramId", v.getParamId());
            row.put("paramName", v.getParamName());
            row.put("inputType", v.getInputType());
            row.put("unit", v.getUnit());
            row.put("actualValue", v.getActualValue());
            row.put("result", v.getResult());
            row.put("sortOrder", v.getSortOrder());
            row.put("notes", v.getNotes());
            // Load items if type=list
            if ("list".equals(v.getInputType())) {
                List<QcSessionValueItem> items = itemMapper.selectList(
                    new QueryWrapper<QcSessionValueItem>().eq("value_id", v.getId()).orderByAsc("seq_no"));
                row.put("items", items);
            }
            result.add(row);
        }
        return result;
    }

    @Override
    public Map<String, Object> getDetail(String sessionId) {
        Map<String, Object> result = new HashMap<>();
        result.put("session", this.getById(sessionId));
        result.put("values", getValues(sessionId));
        return result;
    }

    @Override
    public List<QcSession> listByWorkOrder(String workOrderId) {
        return this.list(new QueryWrapper<QcSession>().eq("work_order_id", workOrderId).orderByAsc("create_time"));
    }

    @SuppressWarnings("unchecked")
    private void saveValues(String sessionId, List<Map<String, Object>> values) {
        if (values == null || values.isEmpty()) return;
        int order = 1;
        for (Map<String, Object> vMap : values) {
            QcSessionValue value = new QcSessionValue();
            value.setId(UUID.randomUUID().toString());
            value.setSessionId(sessionId);
            value.setParamId((String) vMap.get("paramId"));
            value.setParamName((String) vMap.get("paramName"));
            value.setInputType((String) vMap.get("inputType"));
            value.setUnit((String) vMap.get("unit"));
            value.setResult((String) vMap.get("result"));
            value.setNotes((String) vMap.get("notes"));
            value.setSortOrder(order++);

            if ("list".equals(value.getInputType())) {
                value.setActualValue(null);
                valueMapper.insert(value);
                // Save list items
                List<Map<String, Object>> items = (List<Map<String, Object>>) vMap.get("items");
                if (items != null) {
                    int seq = 1;
                    for (Map<String, Object> itemMap : items) {
                        QcSessionValueItem item = new QcSessionValueItem();
                        item.setId(UUID.randomUUID().toString());
                        item.setValueId(value.getId());
                        item.setSeqNo(seq++);
                        item.setMeasuredValue((String) itemMap.get("measuredValue"));
                        item.setResult((String) itemMap.get("result"));
                        item.setNotes((String) itemMap.get("notes"));
                        itemMapper.insert(item);
                    }
                }
            } else {
                value.setActualValue((String) vMap.get("actualValue"));
                valueMapper.insert(value);
            }
        }
    }
}
