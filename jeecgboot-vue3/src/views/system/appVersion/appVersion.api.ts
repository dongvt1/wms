import { defHttp } from '/@/utils/http/axios';

enum Api {
  //QueryappVersion
  queryAppVersion = '/sys/version/app3version',
  //saveappVersion
  saveAppVersion = '/sys/version/saveVersion',
}
/**
 * QueryAPPVersion
 * @param params
 */
export const queryAppVersion = (params) => defHttp.get({ url: Api.queryAppVersion, params });
/**
 * saveAPPVersion
 * @param params
 */
export const saveAppVersion = (params) => {
  return defHttp.post({ url: Api.saveAppVersion, params });
};
