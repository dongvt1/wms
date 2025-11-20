package org.jeecg.common.online.api;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.system.vo.DictModel;

import java.util.List;
import java.util.Map;

/**
 * form designer【Online】translateAPIinterface
 *
 * @author sunjianlei
 */
public interface IOnlineBaseExtApi {

    /**
     * 【Online】 form designer专用：Synchronously add
     * @param tableName table name
     * @param jsonObject
     * @throws Exception
     * @return String
     */
    String cgformPostCrazyForm(String tableName, JSONObject jsonObject) throws Exception;

    /**
     * 【Online】 form designer专用：Synchronous editing
     * @param tableName table name
     * @param jsonObject
     * @throws Exception
     * @return String
     */
    String cgformPutCrazyForm(String tableName, JSONObject jsonObject) throws Exception;

    /**
     * onlineform delete data
     *
     * @param cgformCode Onlineformcode
     * @param dataIds    dataID，comma separable
     * @return
     */
    String cgformDeleteDataByCode(String cgformCode, String dataIds);

    /**
     * passonlinetable namecheck询data，同时check询出子表的data
     *
     * @param tableName onlinetable name
     * @param dataIds   onlinedataID
     * @return
     */
    JSONObject cgformQueryAllDataByTableName(String tableName, String dataIds);

    /**
     * right cgreportGetData Optimize the return value of，Encapsulation DictModel gather
     * @param code
     * @param dictCode
     * @param dataList
     * @param dictText dictionary text
     * @return
     */
    List<DictModel> cgreportGetDataPackage(String code, String dictText, String dictCode, String dataList);

    /**
     * 【cgreport】pass head code Get sqlstatement，并执行该statement返回check询data
     *
     * @param code     ReportCode，If it is not transmittedID就passcodecheck
     * @param forceKey
     * @param dataList
     * @return
     */
    Map<String, Object> cgreportGetData(String code, String forceKey, String dataList);

}
