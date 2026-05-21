package org.jeecg.common.online.api;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.constant.ServiceNameConstants;
import org.jeecg.common.online.api.factory.OnlineBaseExtApiFallbackFactory;
import org.jeecg.common.system.vo.DictModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Description: 【Online】Feign APIinterface
 *
 * @ConditionalOnMissingClass("org.jeecg.modules.online.cgform.service.impl.OnlineBaseExtApiImpl") => When there is an implementation class，Not instantiatedFeigninterface
 * @author: jeecg-boot
 */
@Component
//@FeignClient(contextId = "onlineBaseRemoteApi", value = ServiceNameConstants.SERVICE_ONLINE, fallbackFactory = OnlineBaseExtApiFallbackFactory.class)
@FeignClient(contextId = "onlineBaseRemoteApi", value = ServiceNameConstants.SERVICE_SYSTEM, fallbackFactory = OnlineBaseExtApiFallbackFactory.class)
@ConditionalOnMissingClass("org.jeecg.modules.online.cgform.service.impl.OnlineBaseExtApiImpl")
public interface IOnlineBaseExtApi {

    /**
     * 【Online】 Form designer only：Synchronously add
     * @param tableName table name
     * @param jsonObject
     * @throws Exception
     * @return String
     */
    @PostMapping(value = "/online/api/cgform/crazyForm/{name}")
    String cgformPostCrazyForm(@PathVariable("name") String tableName, @RequestBody JSONObject jsonObject) throws Exception;

    /**
     * 【Online】 Form designer only：Synchronous editing
     * @param tableName table name
     * @param jsonObject
     * @throws Exception
     * @return String
     */
    @PutMapping(value = "/online/api/cgform/crazyForm/{name}")
    String cgformPutCrazyForm(@PathVariable("name") String tableName, @RequestBody JSONObject jsonObject) throws Exception;

    /**
     * passonlinetable namecheck询data，Query the data of sub-tables at the same time
     *
     * @param tableName onlinetable name
     * @param dataIds   onlinedataID
     * @return
     */
    @GetMapping(value = "/online/api/cgform/queryAllDataByTableName")
    JSONObject cgformQueryAllDataByTableName(@RequestParam("tableName") String tableName, @RequestParam("dataIds") String dataIds);

    /**
     * onlineform删除data
     *
     * @param cgformCode Onlineformcode
     * @param dataIds    dataID，comma separable
     * @return
     */
    @DeleteMapping("/online/api/cgform/cgformDeleteDataByCode")
    String cgformDeleteDataByCode(@RequestParam("cgformCode") String cgformCode, @RequestParam("dataIds") String dataIds);

    /**
     * 【cgreport】pass head code Get sqlstatement，并执行该statement返回check询data
     *
     * @param code     ReportCode，If it is not transmittedID就passcodecheck
     * @param forceKey
     * @param dataList
     * @return
     */
    @GetMapping("/online/api/cgreportGetData")
    Map<String, Object> cgreportGetData(@RequestParam("code") String code, @RequestParam("forceKey") String forceKey, @RequestParam("dataList") String dataList);

    /**
     * 【cgreport】right cgreportGetData Optimize the return value of，Encapsulation DictModel gather
     * @param code
     * @param dictText
     * @param dictCode
     * @param dataList
     * @return
     */
    @GetMapping("/online/api/cgreportGetDataPackage")
    List<DictModel> cgreportGetDataPackage(@RequestParam("code") String code, @RequestParam("dictText") String dictText, @RequestParam("dictCode") String dictCode, @RequestParam("dataList") String dataList);

}
