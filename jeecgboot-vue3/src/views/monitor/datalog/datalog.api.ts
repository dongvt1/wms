import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/sys/dataLog/list',
  queryDataVerList = '/sys/dataLog/queryDataVerList',
  queryCompareList = '/sys/dataLog/queryCompareList',
}

/**
 * Query data log list
 * @param params
 */
export const getDataLogList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * Query data log list
 * @param params
 */
export const queryDataVerList = (params) => {
  return defHttp.get({ url: Api.queryDataVerList, params });
};

/**
 * Query and compare data
 * @param params
 */
export const queryCompareList = (params) => {
  return defHttp.get({ url: Api.queryCompareList, params });
};
