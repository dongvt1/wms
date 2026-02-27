package qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import qms.entity.QcStage;
import qms.entity.QcStageParam;
import qms.mapper.QcStageMapper;
import qms.mapper.QcStageParamMapper;
import qms.service.QcStageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class QcStageServiceImpl extends ServiceImpl<QcStageMapper, QcStage>
        implements QcStageService {

    @Autowired
    private QcStageParamMapper paramMapper;

    @Override
    public String generateStageCode() {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        QueryWrapper<QcStage> qw = new QueryWrapper<>();
        qw.likeRight("stage_code", "STG" + dateStr).orderByDesc("stage_code").last("LIMIT 1");
        QcStage last = this.getOne(qw);
        int seq = 1;
        if (last != null) {
            try { seq = Integer.parseInt(last.getStageCode().substring(last.getStageCode().length() - 3)) + 1; }
            catch (NumberFormatException e) { seq = 1; }
        }
        return "STG" + dateStr + String.format("%03d", seq);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithParams(QcStage stage, List<QcStageParam> params) {
        this.save(stage);
        saveParams(stage.getId(), params);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithParams(QcStage stage, List<QcStageParam> params) {
        this.updateById(stage);
        // Replace all params
        paramMapper.delete(new QueryWrapper<QcStageParam>().eq("stage_id", stage.getId()));
        saveParams(stage.getId(), params);
    }

    @Override
    public List<QcStageParam> getParams(String stageId) {
        return paramMapper.selectList(
            new QueryWrapper<QcStageParam>().eq("stage_id", stageId).orderByAsc("sort_order")
        );
    }

    @Override
    public Map<String, Object> getDetail(String stageId) {
        Map<String, Object> result = new HashMap<>();
        result.put("stage", this.getById(stageId));
        result.put("params", getParams(stageId));
        return result;
    }

    @Override
    public List<QcStage> listActive() {
        return this.list(new QueryWrapper<QcStage>().eq("status", "active").orderByAsc("sort_order"));
    }

    private void saveParams(String stageId, List<QcStageParam> params) {
        if (params == null || params.isEmpty()) return;
        int order = 1;
        for (QcStageParam p : params) {
            p.setId(UUID.randomUUID().toString());
            p.setStageId(stageId);
            if (p.getSortOrder() == null) p.setSortOrder(order++);
            paramMapper.insert(p);
        }
    }
}
