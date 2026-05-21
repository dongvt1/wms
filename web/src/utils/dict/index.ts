import { defHttp } from '/@/utils/http/axios';
import { useUserStore } from '/@/store/modules/user';
import { getAuthCache } from '/@/utils/auth';
import { DB_DICT_DATA_KEY } from '/@/enums/cacheEnum';

/**
 * Get dictionary configuration from cache
 * @param code
 */
export const getDictItemsByCode = (code) => {
  // update-begin--author:liaozhiyang---date:20230908---for：【QQYUN-6417】The problem of slow dictionary in production environment
  const userStore = useUserStore();
  const dictItems = userStore.getAllDictItems;
  if (null != dictItems && typeof dictItems === 'object' && dictItems[code]) {
    return dictItems[code];
  }
  //update-begin-author:liusq---date:2023-10-13--for: 【issues/777】list Classification dictionary is not displayed
  //Compatible with the old writing method
  if (getAuthCache(DB_DICT_DATA_KEY) && getAuthCache(DB_DICT_DATA_KEY)[code]) {
    return getAuthCache(DB_DICT_DATA_KEY)[code];
  }
  //update-end-author:liusq---date:2023-10-13--for:【issues/777】list Classification dictionary is not displayed

  // update-end--author:liaozhiyang---date:20230908---for：【QQYUN-6417】The problem of slow dictionary in production environment

};
/**
 * Get from cachePopDictionary configuration
 * @param text
 * @param code
 */
export const getPopDictByCode = (text, codeStr) => {
  const [code, dictCode, dictText] = codeStr.split(',');
  if (!code || !dictCode || !dictText) {
    return [];
  }
  return defHttp.get(
    { url: `/online/api/cgreportGetDataPackage`, params: { code, dictText, dictCode, dataList: text } },
    { isTransformResponse: false }
  );
};
/**
 * Get dictionary array
 * @param dictCode dictionaryCode
 * @return List<Map>
 */
export const initDictOptions = (code) => {
  //1.优先从缓存中读取Dictionary configuration
  if (getDictItemsByCode(code)) {
    return new Promise((resolve, reject) => {
      resolve(getDictItemsByCode(code));
    });
  }
  //2.Get dictionary array
  //update-begin-author:taoyan date:2022-6-21 for: dictionary数据请求前将参数编码处理，But it cannot be directly encoded，Because it may have been encoded before
  if (code.indexOf(',') > 0 && code.indexOf(' ') > 0) {
    // After encoding it is similar tosys_user%20where%20username%20like%20xxx' does not contain spaces,It is judged here that if there are spaces and commas, it needs to be encoded.
    code = encodeURI(code);
  }
  //update-end-author:taoyan date:2022-6-21 for: dictionary数据请求前将参数编码处理，But it cannot be directly encoded，Because it may have been encoded before
  return defHttp.get({ url: `/sys/dict/getDictItems/${code}` });
};
/**
 * Get dictionary array
 * @param code dictionaryCode
 * @param params query parameters
 * @param options Query configuration
 * @return List<Map>
 */
export const ajaxGetDictItems = (code, params, options?) => defHttp.get({ url: `/sys/dict/getDictItems/${code}`, params }, options);
