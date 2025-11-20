package org.jeecg.common.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description: dictionaryaopkind
 * @Author: dangzhenghui
 * @Date: 2019-3-17 21:50
 * @Version: 1.0
 */
@Aspect
@Component
@Slf4j
public class DictAspect {
    @Lazy
    @Autowired
    private CommonAPI commonApi;
    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String JAVA_UTIL_DATE = "java.util.Date";

    /**
     * Define cut pointsPointcut
     */
    @Pointcut("(@within(org.springframework.web.bind.annotation.RestController) || " +
            "@within(org.springframework.stereotype.Controller) || @annotation(org.jeecg.common.aspect.annotation.AutoDict)) " +
            "&& execution(public org.jeecg.common.api.vo.Result org.jeecg..*.*(..))")
    public void excudeService() {
    }

    @Around("excudeService()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
    	long time1=System.currentTimeMillis();	
        Object result = pjp.proceed();
        long time2=System.currentTimeMillis();
        log.debug("GetJSONdata time consuming："+(time2-time1)+"ms");
        long start=System.currentTimeMillis();
        result=this.parseDictText(result);
        long end=System.currentTimeMillis();
        log.debug("注入dictionary到JSONdata  time consuming"+(end-start)+"ms");
        return result;
    }

    /**
     * The object returned by this method isResult ofIPageof分页列表data进行动态dictionary注入
     * dictionary注入实现 通过对实体kind添加annotation@dict 来标识需要ofdictionary内容,dictionary分为单dictionarycodeThat’s it ，tabledictionary code table textUse in conjunction with the originaljeecgof用法相同
     * Example isSysUser   The fields aresex Added annotation@Dict(dicCode = "sex") 会existdictionary服务立马查出来对应oftext and then requestlistof时候将这个dictionarytext，Field name added_dictTextform returns to frontend
     * 例输入当前return值of就会多出一个sex_dictTextField
     * {
     *      sex:1,
     *      sex_dictText:"male"
     * }
     * Get the value directly from the front endsext_dictTextexisttableinside面无需再进行前端ofdictionary转换了
     *  customRender:function (text) {
     *               if(text==1){
     *                 return "male";
     *               }else if(text==2){
     *                 return "female";
     *               }else{
     *                 return text;
     *               }
     *             }
     *             at presentvue是这么进行dictionary渲染到table上of多了就很麻烦了 这个直接exist服务端渲染完成前端可以直接用
     * @param result
     */
    private Object parseDictText(Object result) {
        //if (result instanceof Result) {
        if (true) {
            if (((Result) result).getResult() instanceof IPage) {
                List<JSONObject> items = new ArrayList<>();

                //step.1 Filtered out and added Dict annotationofField列表
                List<Field> dictFieldList = new ArrayList<>();
                // dictionarydata列表， key = dictionarycode，value=data列表
                Map<String, List<String>> dataListMap = new HashMap<>(5);
                //Get result set
                List<Object> records=((IPage) ((Result) result).getResult()).getRecords();
                //update-begin--Author:zyf -- Date:20220606 ----for：【VUEN-1230】 判断是否含有dictionaryannotation,No annotations are returned-----
                Boolean hasDict= checkHasDict(records);
                if(!hasDict){
                    return result;
                }

                log.debug(" __ 进入dictionary翻译切面 DictAspect —— " );
                //update-end--Author:zyf -- Date:20220606 ----for：【VUEN-1230】 判断是否含有dictionaryannotation,No annotations are returned-----
                for (Object record : records) {
                    String json="{}";
                    try {
                        //update-begin--Author:zyf -- Date:20220531 ----for：【issues/#3629】 DictAspect JacksonSerialization error-----
                        //solve@JsonFormatannotation解析不了of问题详见SysAnnouncementkindof@JsonFormat
                         json = objectMapper.writeValueAsString(record);
                        //update-end--Author:zyf -- Date:20220531 ----for：【issues/#3629】 DictAspect JacksonSerialization error-----
                    } catch (JsonProcessingException e) {
                        log.error("jsonParsing failed"+e.getMessage(),e);
                    }
                    //update-begin--Author:scott -- Date:20211223 ----for：【issues/3303】restcontrollerreturnjsondata后keyOut of order -----
                    JSONObject item = JSONObject.parseObject(json, Feature.OrderedField);
                    //update-end--Author:scott -- Date:20211223 ----for：【issues/3303】restcontrollerreturnjsondata后keyOut of order -----

                    //update-begin--Author:scott -- Date:20190603 ----for：solve继承实体Field无法翻译问题------
                    //for (Field field : record.getClass().getDeclaredFields()) {
                    // 遍历所有Field，把dictionaryCodetake out，put in map inside
                    for (Field field : oConvertUtils.getAllFields(record)) {
                        String value = item.getString(field.getName());
                        if (oConvertUtils.isEmpty(value)) {
                            continue;
                        }
                    //update-end--Author:scott  -- Date:20190603 ----for：solve继承实体Field无法翻译问题------
                        if (field.getAnnotation(Dict.class) != null) {
                            if (!dictFieldList.contains(field)) {
                                dictFieldList.add(field);
                            }
                            String code = field.getAnnotation(Dict.class).dicCode();
                            String text = field.getAnnotation(Dict.class).dicText();
                            String table = field.getAnnotation(Dict.class).dictTable();
                            //update-begin---author:chenrui ---date:20231221  for：[issues/#5643]solve分布式下表dictionary跨库无法查询问题------------
                            String dataSource = field.getAnnotation(Dict.class).ds();
                            //update-end---author:chenrui ---date:20231221  for：[issues/#5643]solve分布式下表dictionary跨库无法查询问题------------
                            List<String> dataList;
                            String dictCode = code;
                            if (!StringUtils.isEmpty(table)) {
                                //update-begin---author:chenrui ---date:20231221  for：[issues/#5643]solve分布式下表dictionary跨库无法查询问题------------
                                dictCode = String.format("%s,%s,%s,%s", table, text, code, dataSource);
                                //update-end---author:chenrui ---date:20231221  for：[issues/#5643]solve分布式下表dictionary跨库无法查询问题------------
                            }
                            dataList = dataListMap.computeIfAbsent(dictCode, k -> new ArrayList<>());
                            this.listAddAllDeduplicate(dataList, Arrays.asList(value.split(",")));
                        }
                        //datekind型默认转换stringFormat date
                        //update-begin--Author:zyf -- Date:20220531 ----for：【issues/#3629】 DictAspect JacksonSerialization error-----
                        //if (JAVA_UTIL_DATE.equals(field.getType().getName())&&field.getAnnotation(JsonFormat.class)==null&&item.get(field.getName())!=null){
                            //SimpleDateFormat aDate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            // item.put(field.getName(), aDate.format(new Date((Long) item.get(field.getName()))));
                        //}
                        //update-end--Author:zyf -- Date:20220531 ----for：【issues/#3629】 DictAspect JacksonSerialization error-----
                    }
                    items.add(item);
                }

                //step.2 Call translation method，One-time translation
                Map<String, List<DictModel>> translText = this.translateAllDict(dataListMap);

                //step.3 将翻译结果填充到return结果inside
                for (JSONObject record : items) {
                    for (Field field : dictFieldList) {
                        String code = field.getAnnotation(Dict.class).dicCode();
                        String text = field.getAnnotation(Dict.class).dicText();
                        String table = field.getAnnotation(Dict.class).dictTable();
                        //update-begin---author:chenrui ---date:20231221  for：[issues/#5643]solve分布式下表dictionary跨库无法查询问题------------
                        // 自定义ofdictionary表data源
                        String dataSource = field.getAnnotation(Dict.class).ds();
                        //update-end---author:chenrui ---date:20231221  for：[issues/#5643]solve分布式下表dictionary跨库无法查询问题------------
                        String fieldDictCode = code;
                        if (!StringUtils.isEmpty(table)) {
                            //update-begin---author:chenrui ---date:20231221  for：[issues/#5643]solve分布式下表dictionary跨库无法查询问题------------
                            fieldDictCode = String.format("%s,%s,%s,%s", table, text, code, dataSource);
                            //update-end---author:chenrui ---date:20231221  for：[issues/#5643]solve分布式下表dictionary跨库无法查询问题------------
                        }

                        String value = record.getString(field.getName());
                        if (oConvertUtils.isNotEmpty(value)) {
                            List<DictModel> dictModels = translText.get(fieldDictCode);
                            if(dictModels==null || dictModels.size()==0){
                                continue;
                            }

                            String textValue = this.translDictText(dictModels, value);
                            log.debug(" dictionaryVal : " + textValue);
                            log.debug(" __翻译dictionaryField__ " + field.getName() + CommonConstant.DICT_TEXT_SUFFIX + "： " + textValue);

                            // TODO-sun test output，To be deleted
                            log.debug(" ---- dictCode: " + fieldDictCode);
                            log.debug(" ---- value: " + value);
                            log.debug(" ----- text: " + textValue);
                            log.debug(" ---- dictModels: " + JSON.toJSONString(dictModels));

                            record.put(field.getName() + CommonConstant.DICT_TEXT_SUFFIX, textValue);
                        }
                    }
                }

                ((IPage) ((Result) result).getResult()).setRecords(items);
            }

        }
        return result;
    }

    /**
     * list Remove duplicates and add
     */
    private void listAddAllDeduplicate(List<String> dataList, List<String> addList) {
        // filter outdataList中没有ofdata
        List<String> filterList = addList.stream().filter(i -> !dataList.contains(i)).collect(Collectors.toList());
        dataList.addAll(filterList);
    }

    /**
     * 一次性把所有ofdictionary都翻译了
     * 1.  所有of普通datadictionaryof所有data只执行一次SQL
     * 2.  表dictionary相同of所有data只执行一次SQL
     * @param dataListMap
     * @return
     */
    private Map<String, List<DictModel>> translateAllDict(Map<String, List<String>> dataListMap) {
        // 翻译后ofdictionary文本，key=dictCode
        Map<String, List<DictModel>> translText = new HashMap<>(5);
        // 需要翻译ofdata（Some can be obtained fromrediscache中Get，就不走data库查询）
        List<String> needTranslData = new ArrayList<>();
        //step.1 Pass firstredis中Getcachedictionarydata
        for (String dictCode : dataListMap.keySet()) {
            List<String> dataList = dataListMap.get(dictCode);
            if (dataList.size() == 0) {
                continue;
            }
            // 表dictionary需要翻译ofdata
            List<String> needTranslDataTable = new ArrayList<>();
            for (String s : dataList) {
                String data = s.trim();
                if (data.length() == 0) {
                    continue; //skip loop
                }
                if (dictCode.contains(",")) {
                    String keyString = String.format("sys:cache:dictTable::SimpleKey [%s,%s]", dictCode, data);
                    if (redisTemplate.hasKey(keyString)) {
                        try {
                            String text = oConvertUtils.getString(redisTemplate.opsForValue().get(keyString));
                            List<DictModel> list = translText.computeIfAbsent(dictCode, k -> new ArrayList<>());
                            list.add(new DictModel(data, text));
                        } catch (Exception e) {
                            log.warn(e.getMessage());
                        }
                    } else if (!needTranslDataTable.contains(data)) {
                        // Remove duplicates and add
                        needTranslDataTable.add(data);
                    }
                } else {
                    String keyString = String.format("sys:cache:dict::%s:%s", dictCode, data);
                    if (redisTemplate.hasKey(keyString)) {
                        try {
                            String text = oConvertUtils.getString(redisTemplate.opsForValue().get(keyString));
                            List<DictModel> list = translText.computeIfAbsent(dictCode, k -> new ArrayList<>());
                            list.add(new DictModel(data, text));
                        } catch (Exception e) {
                            log.warn(e.getMessage());
                        }
                    } else if (!needTranslData.contains(data)) {
                        // Remove duplicates and add
                        needTranslData.add(data);
                    }
                }

            }
            //step.2 调用data库翻译表dictionary
            if (needTranslDataTable.size() > 0) {
                String[] arr = dictCode.split(",");
                String table = arr[0], text = arr[1], code = arr[2];
                String values = String.join(",", needTranslDataTable);
                //update-begin---author:chenrui ---date:20231221  for：[issues/#5643]solve分布式下表dictionary跨库无法查询问题------------
                // 自定义ofdata源
                String dataSource = null;
                if (arr.length > 3) {
                    dataSource = arr[3];
                }
                //update-end---author:chenrui ---date:20231221  for：[issues/#5643]solve分布式下表dictionary跨库无法查询问题------------
                log.debug("translateDictFromTableByKeys.dictCode:" + dictCode);
                log.debug("translateDictFromTableByKeys.values:" + values);
                //update-begin---author:chenrui ---date:20231221  for：[issues/#5643]solve分布式下表dictionary跨库无法查询问题------------
                
                //update-begin---author:wangshuai---date:2024-01-09---for:An empty error is reported under microservices. There are no parameters and an empty string needs to be passed.---
                if(null == dataSource){
                    dataSource = "";
                }
                //update-end---author:wangshuai---date:2024-01-09---for:An empty error is reported under microservices. There are no parameters and an empty string needs to be passed.---
                
                List<DictModel> texts = commonApi.translateDictFromTableByKeys(table, text, code, values, dataSource);
                //update-end---author:chenrui ---date:20231221  for：[issues/#5643]solve分布式下表dictionary跨库无法查询问题------------
                log.debug("translateDictFromTableByKeys.result:" + texts);
                List<DictModel> list = translText.computeIfAbsent(dictCode, k -> new ArrayList<>());
                list.addAll(texts);

                // Do redis cache
                for (DictModel dict : texts) {
                    String redisKey = String.format("sys:cache:dictTable::SimpleKey [%s,%s]", dictCode, dict.getValue());
                    try {
                        // update-begin-author:taoyan date:20211012 for: dictionary表翻译annotationcache未更新 issues/3061
                        // reserve5minute
                        redisTemplate.opsForValue().set(redisKey, dict.getText(), 300, TimeUnit.SECONDS);
                        // update-end-author:taoyan date:20211012 for: dictionary表翻译annotationcache未更新 issues/3061
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }
        }

        //step.3 调用data库进行翻译普通dictionary
        if (needTranslData.size() > 0) {
            List<String> dictCodeList = Arrays.asList(dataListMap.keySet().toArray(new String[]{}));
            // 将不包含逗号ofdictionarycodefilter out来，因为带逗号of是表dictionary，而不是普通ofdatadictionary
            List<String> filterDictCodes = dictCodeList.stream().filter(key -> !key.contains(",")).collect(Collectors.toList());
            String dictCodes = String.join(",", filterDictCodes);
            String values = String.join(",", needTranslData);
            log.debug("translateManyDict.dictCodes:" + dictCodes);
            log.debug("translateManyDict.values:" + values);
            Map<String, List<DictModel>> manyDict = commonApi.translateManyDict(dictCodes, values);
            log.debug("translateManyDict.result:" + manyDict);
            for (String dictCode : manyDict.keySet()) {
                List<DictModel> list = translText.computeIfAbsent(dictCode, k -> new ArrayList<>());
                List<DictModel> newList = manyDict.get(dictCode);
                list.addAll(newList);

                // Do redis cache
                for (DictModel dict : newList) {
                    String redisKey = String.format("sys:cache:dict::%s:%s", dictCode, dict.getValue());
                    try {
                        redisTemplate.opsForValue().set(redisKey, dict.getText());
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }
        }
        return translText;
    }

    /**
     * dictionary值替换文本
     *
     * @param dictModels
     * @param values
     * @return
     */
    private String translDictText(List<DictModel> dictModels, String values) {
        List<String> result = new ArrayList<>();

        // Allow multiple comma separated，Allows passing array objects
        String[] splitVal = values.split(",");
        for (String val : splitVal) {
            String dictText = val;
            for (DictModel dict : dictModels) {
                if (val.equals(dict.getValue())) {
                    dictText = dict.getText();
                    break;
                }
            }
            result.add(dictText);
        }
        return String.join(",", result);
    }

    /**
     *  翻译dictionary文本
     * @param code
     * @param text
     * @param table
     * @param key
     * @return
     */
    @Deprecated
    private String translateDictValue(String code, String text, String table, String key) {
    	if(oConvertUtils.isEmpty(key)) {
    		return null;
    	}
        StringBuffer textValue=new StringBuffer();
        String[] keys = key.split(",");
        for (String k : keys) {
            String tmpValue = null;
            log.debug(" dictionary key : "+ k);
            if (k.trim().length() == 0) {
                continue; //skip loop
            }
            //update-begin--Author:scott -- Date:20210531 ----for： !56 优化微服务应用下存exist表Field需要dictionary翻译时加载缓慢问题-----
            if (!StringUtils.isEmpty(table)){
                log.debug("--DictAspect------dicTable="+ table+" ,dicText= "+text+" ,dicCode="+code);
                String keyString = String.format("sys:cache:dictTable::SimpleKey [%s,%s,%s,%s]",table,text,code,k.trim());
                    if (redisTemplate.hasKey(keyString)){
                    try {
                        tmpValue = oConvertUtils.getString(redisTemplate.opsForValue().get(keyString));
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                }else {
                    tmpValue= commonApi.translateDictFromTable(table,text,code,k.trim());
                }
            }else {
                String keyString = String.format("sys:cache:dict::%s:%s",code,k.trim());
                if (redisTemplate.hasKey(keyString)){
                    try {
                        tmpValue = oConvertUtils.getString(redisTemplate.opsForValue().get(keyString));
                    } catch (Exception e) {
                       log.warn(e.getMessage());
                    }
                }else {
                    tmpValue = commonApi.translateDict(code, k.trim());
                }
            }
            //update-end--Author:scott -- Date:20210531 ----for： !56 优化微服务应用下存exist表Field需要dictionary翻译时加载缓慢问题-----

            if (tmpValue != null) {
                if (!"".equals(textValue.toString())) {
                    textValue.append(",");
                }
                textValue.append(tmpValue);
            }

        }
        return textValue.toString();
    }

    /**
     * 检测return结果集中是否包含Dictannotation
     * @param records
     * @return
     */
    private Boolean checkHasDict(List<Object> records){
        if(oConvertUtils.isNotEmpty(records) && records.size()>0){
            for (Field field : oConvertUtils.getAllFields(records.get(0))) {
                if (oConvertUtils.isNotEmpty(field.getAnnotation(Dict.class))) {
                    return true;
                }
            }
        }
        return false;
    }

}
