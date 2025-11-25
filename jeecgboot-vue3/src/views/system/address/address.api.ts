import { defHttp } from '/@/utils/http/axios';

export enum Api {
  list = '/sys/user/queryByOrgCodeForAddressList',
  positionList = '/sys/position/list',
  queryDepartTreeSync = '/sys/sysDepart/queryDepartAndPostTreeSync',
}
/**
 * Get department tree list
 */
export const queryDepartTreeSync = (params?) => defHttp.get({ url: Api.queryDepartTreeSync, params });
/**
 * Department user information
 */
export const list = (params?) => defHttp.get({ url: Api.list, params });
/**
 * Positionlist
 */
export const positionList = (params?) => defHttp.get({ url: Api.positionList, params });
