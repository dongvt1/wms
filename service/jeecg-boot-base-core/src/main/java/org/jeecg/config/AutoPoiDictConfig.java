package org.jeecg.config;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Resource;

import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.util.oConvertUtils;
import org.jeecgframework.dict.service.AutoPoiDictServiceI;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * describe：AutoPoi ExcelAnnotations support dictionary parameter settings
 *  Example： @Excel(name = "gender", width = 15, dicCode = "sex")
 * 1、When exporting, it will be configured according to the dictionary，value1,2translated into：male、female;
 * 2、When importing，会把male、femaletranslated into1,2Save to database;
 * 
 * @Author:scott 
 * @since：2019-04-09 
 * @Version:1.0
 */
@Slf4j
@Lazy(false)
@Service
public class AutoPoiDictConfig implements AutoPoiDictServiceI {
	final static String EXCEL_SPLIT_TAG = "_";
	final static String TEMP_EXCEL_SPLIT_TAG = "---";

	@Lazy
	@Resource
	private CommonAPI commonApi;

	/**
	 * Search via dictionaryeasypoi，required dictionary text
	 * 
	 * @Author:scott 
	 * @since：2019-04-09
	 * @return
	 */
	@Override
	public String[] queryDict(String dicTable, String dicCode, String dicText) {
		List<String> dictReplaces = new ArrayList<String>();
		List<DictModel> dictList = null;
		// step.1 If there is no dictionary table, use the system dictionary table
		if (oConvertUtils.isEmpty(dicTable)) {
			dictList = commonApi.queryDictItemsByCode(dicCode);
		} else {
			try {
				dicText = oConvertUtils.getString(dicText, dicCode);
				dictList = commonApi.queryTableDictItemsByCode(dicTable, dicText, dicCode);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}
		}


		for (DictModel t : dictList) {
			//update-begin---author:liusq   Date:20230517  for：[issues/4917]excel Export exception---
			if(t!=null && t.getText()!=null && t.getValue()!=null){
			//update-end---author:liusq     Date:20230517  for：[issues/4917]excel Export exception---
				//update-begin---author:scott   Date:20211220  for：[issues/I4MBB3]@Excel dicTextWhen the field value is underlined，Import function is not parsed correctly---
				if(t.getValue().contains(EXCEL_SPLIT_TAG)){
					String val = t.getValue().replace(EXCEL_SPLIT_TAG,TEMP_EXCEL_SPLIT_TAG);
					dictReplaces.add(t.getText() + EXCEL_SPLIT_TAG + val);
				}else{
					dictReplaces.add(t.getText() + EXCEL_SPLIT_TAG + t.getValue());
				}
				//update-end---author:20211220     Date:20211220  for：[issues/I4MBB3]@Excel dicTextWhen the field value is underlined，Import function is not parsed correctly---
			}
		}
		if (dictReplaces != null && dictReplaces.size() != 0) {
			log.info("---AutoPoi--Get_DB_Dict------"+ dictReplaces.toString());
			return dictReplaces.toArray(new String[dictReplaces.size()]);
		}
		return null;
	}
}
