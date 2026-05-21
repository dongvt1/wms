package org.jeecg.modules.demo.online;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Onlineform development demo Example
 *
 * @author sunjianlei
 * @date 2021-12-16
 */
@Slf4j
@RestController("onlCgformDemoController")
@RequestMapping("/demo/online/cgform")
public class OnlCgformDemoController {

    /**
     * Onlineform http Enhance，listEnhanceExample
     * @param params
     * @return
     */
    @PostMapping("/enhanceJavaListHttp")
    public Result<?> enhanceJavaListHttp(@RequestBody JSONObject params) {
        log.info(" --- params：" + params.toJSONString());
        JSONArray dataList = params.getJSONArray("dataList");
        List<DictModel> dict = virtualDictData();
        for (int i = 0; i < dataList.size(); i++) {
            JSONObject record = dataList.getJSONObject(i);
            String province = record.getString("province");
            if (province == null) {
                continue;
            }
            String text = dict.stream()
                    .filter(p -> province.equals(p.getValue()))
                    .map(DictModel::getText)
                    .findAny()
                    .orElse(province);
            record.put("province", text);
        }
        Result<?> res = Result.OK(dataList);
        res.setCode(1);
        return res;
    }

    /**
     * Simulate dictionary data
     *
     * @return
     */
    private List<DictModel> virtualDictData() {
        List<DictModel> dict = new ArrayList<>();
        dict.add(new DictModel("bj", "Beijing"));
        dict.add(new DictModel("sd", "Shandong"));
        dict.add(new DictModel("ah", "Anhui"));
        return dict;
    }


    /**
     * Onlineform http Enhance，add、editEnhanceExample
     * @param params
     * @return
     */
    @PostMapping("/enhanceJavaHttp")
    public Result<?> enhanceJavaHttp(@RequestBody JSONObject params) {
        log.info(" --- params：" + params.toJSONString());
        String tableName = params.getString("tableName");
        JSONObject record = params.getJSONObject("record");
        /*
         * Business scenario one： 获取提交form数据，Perform other business related operations
         * （for example：According to the warehouse receipt，Synchronize inventory changes）
         */
        log.info(" --- tableName：" + tableName);
        log.info(" --- row data：" + record.toJSONString());
        /*
         * Business scenario two： 保存数据之前进row data的校验
         * Just return the error status directly
         */
        String phone = record.getString("phone");
        if (oConvertUtils.isEmpty(phone)) {
            return Result.error("Mobile phone number cannot be empty！");
        }
        /*
         * Business scenario three： Save data and process data
         * Direct operation record That’s it
         */
        record.put("phone", "010-" + phone);

        /* Other business scenarios can be implemented by yourself */

        // Return to scene one： wrong record without making any modifications，You can return directly code，
        // return 0 = Discard current data
        // return 1 = Add current data
        // return 2 = Modify current data TODO（？）Doubtful
//		 return Result.OK(1);

        // return场景二： need to record When making changes，需要return一个JSONObjectobject（orMapOK）
        JSONObject res = new JSONObject();
        res.put("code", 1);
        // Will record return以进行修改
        res.put("record", record);
        // TODO don't want code concept
        return Result.OK(res);
    }

}
