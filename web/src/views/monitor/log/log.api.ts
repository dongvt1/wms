import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/sys/log/list',
  exportXls = '/sys/log/exportXls',
}

/**
 * Query log list
 * @param params
 */
export const getLogList = (params) => {
  return defHttp.get({ url: Api.list, params });
};


/**
 * Exportapi
 * @param params
 */
export const getExportUrl = Api.exportXls;
