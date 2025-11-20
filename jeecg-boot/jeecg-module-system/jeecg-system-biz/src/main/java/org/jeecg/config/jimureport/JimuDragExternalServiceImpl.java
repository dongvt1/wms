package org.jeecg.config.jimureport;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.dto.LogDTO;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.drag.service.IOnlDragExternalService;
import org.jeecg.modules.drag.vo.DragDictModel;
import org.jeecg.modules.drag.vo.DragLogDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: Dictionary processing
 * @Author: lsq
 * @Date:2023-01-09
 * @Version:V1.0
 */
@Slf4j
@Service("onlDragExternalServiceImpl")
public class JimuDragExternalServiceImpl implements IOnlDragExternalService {

    @Autowired
    @Lazy
    private BaseCommonService baseCommonService;

    @Autowired
    @Lazy
    private ISysBaseAPI sysBaseApi;
    /**
     *  According to multiple dictionariescodeQuery multiple dictionary items
     * @param codeList
     * @return key = dictCode ； value=corresponding dictionary entry
     */
    @Override
    public Map<String, List<DragDictModel>> getManyDictItems(List<String> codeList, List<JSONObject> tableDictList) {
        Map<String, List<DragDictModel>> manyDragDictItems  = new HashMap<>();
        if(!CollectionUtils.isEmpty(codeList)){
            Map<String, List<DictModel>> dictItemsMap = sysBaseApi.getManyDictItems(codeList);
            dictItemsMap.forEach((k,v)->{
                List<DragDictModel> dictItems  = new ArrayList<>();
                v.forEach(dictItem->{
                    DragDictModel dictModel = new DragDictModel();
                    BeanUtils.copyProperties(dictItem,dictModel);
                    dictItems.add(dictModel);
                });
                manyDragDictItems.put(k,dictItems);
            });
        }

        if(!CollectionUtils.isEmpty(tableDictList)){
            tableDictList.forEach(item->{
                List<DragDictModel> dictItems  = new ArrayList<>();
                JSONObject object = JSONObject.parseObject(item.toString());
                String dictField = object.getString("dictField");
                String dictTable = object.getString("dictTable");
                String dictText = object.getString("dictText");
                String fieldName = object.getString("fieldName");
                List<DictModel> dictItemsList = sysBaseApi.queryTableDictItemsByCode(dictTable,dictText,dictField);
                dictItemsList.forEach(dictItem->{
                    DragDictModel dictModel = new DragDictModel();
                    BeanUtils.copyProperties(dictItem,dictModel);
                    dictItems.add(dictModel);
                });
                manyDragDictItems.put(fieldName,dictItems);
            });
        }
        return manyDragDictItems;
    }

    /**
     *
     * @param dictCode
     * @return
     */
    @Override
    public List<DragDictModel> getDictItems(String dictCode) {
        List<DragDictModel> dictItems  = new ArrayList<>();
        if(oConvertUtils.isNotEmpty(dictCode)){
            List<DictModel> dictItemsList = sysBaseApi.getDictItems(dictCode);
            dictItemsList.forEach(dictItem->{
                DragDictModel dictModel = new DragDictModel();
                BeanUtils.copyProperties(dictItem,dictModel);
                dictItems.add(dictModel);
            });
        }
        return dictItems;
    }

    /**
     * add log
     * @param dragLogDTO
     */
    @Override
    public void addLog(DragLogDTO dragLogDTO) {
        if(oConvertUtils.isNotEmpty(dragLogDTO)){
            LogDTO dto = new LogDTO();
            BeanUtils.copyProperties(dragLogDTO,dto);
            baseCommonService.addLog(dto);
        }
    }

    /**
     * save log
     * @param logMsg
     * @param logType
     * @param operateType
     */
    @Override
    public void addLog(String logMsg, int logType, int operateType) {
        baseCommonService.addLog(logMsg,logType,operateType);
    }
}