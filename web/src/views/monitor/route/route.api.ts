import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/sys/gatewayRoute/list',
  deleteList = '/sys/gatewayRoute/deleteList',
  save = '/sys/gatewayRoute/add',
  edit = '/sys/gatewayRoute/updateAll',
  delete = '/sys/gatewayRoute/delete',

  copyRoute = '/sys/gatewayRoute/copyRoute',
  batchPutRecycleBin = '/sys/gatewayRoute/putRecycleBin',
  batchDeleteRecycleBin = '/sys/gatewayRoute/deleteRecycleBin',
}

/**
 * Query route list
 * @param params
 */
export const getRouteList = (params) => {
  return defHttp.get({ url: Api.list, params });
};
/**
 * Query the tombstone routing list
 * @param params
 */
export const deleteRouteList = (params) => {
  return defHttp.get({ url: Api.deleteList, params });
};

/**
 * Save or update routes
 * @param params
 */
export const saveOrUpdateRoute = (params) => {
  return defHttp.post({ url: Api.edit, params });
};

/**
 * Delete route
 * @param params
 */
export const deleteRoute = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * Recycle Bin Restore
 * @param params
 */
export const putRecycleBin = (params, handleSuccess) => {
  return defHttp.put({ url: Api.batchPutRecycleBin, params }).then(() => {
    handleSuccess();
  });
};
/**
 * Recycle bin delete
 * @param params
 */
export const deleteRecycleBin = (params, handleSuccess) => {
  return defHttp.delete({ url: `${Api.batchDeleteRecycleBin}?ids=${params.ids}` }).then(() => {
    handleSuccess();
  });
};
/**
 * copy
 */
export const copyRoute = (params, handleSuccess) => {
  return defHttp.get({ url: Api.copyRoute, params }).then(() => {
    handleSuccess();
  });
};
