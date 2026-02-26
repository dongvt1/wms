import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/sys/annountCement/list',
  save = '/sys/annountCement/add',
  edit = '/sys/annountCement/edit',
  delete = '/sys/annountCement/delete',
  queryById = '/sys/annountCement/queryById',
  deleteBatch = '/sys/annountCement/deleteBatch',
  exportXls = '/sys/annountCement/exportXls',
  importExcel = '/sys/annountCement/importExcel',
  releaseData = '/sys/annountCement/doReleaseData',
  reovkeData = '/sys/annountCement/doReovkeData',
  editIzTop = '/sys/annountCement/editIzTop',
  addVisitsNum = '/sys/annountCement/addVisitsNumber',
  tempList = '/sys/message/sysMessageTemplate/list',
}

/**
 * Exporturl
 */
export const getExportUrl = Api.exportXls;
/**
 * importurl
 */
export const getImportUrl = Api.importExcel;
/**
 * Query message list
 * @param params
 */
export const getList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * Save or update announcement
 * @param params
 */
export const saveOrUpdate = (params, isUpdate) => {
  const url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};

/**
 * Delete notice
 * @param params
 */
export const deleteNotice = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * Pin to the topEdit
 * @param params
 */
export const editIzTop = (params, handleSuccess) => {
  return defHttp.post({ url: Api.editIzTop, data: params }).then(() => {
    handleSuccess();
  });
};

/**
 * Bulk message announcement
 * @param params
 */
export const batchDeleteNotice = (params) => defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true });

/**
 * release
 * @param id
 */
export const doReleaseData = (params) => defHttp.get({ url: Api.releaseData, params });
/**
 * Cancel
 * @param id
 */
export const doReovkeData = (params) => defHttp.get({ url: Api.reovkeData, params });
/**
 * New visits
 * @param id
 */
export const addVisitsNum = (params) => defHttp.get({ url: Api.addVisitsNum, params }, { successMessageMode: 'none' });
/**
 * according toIDQuery data
 * @param id
 */
export const queryById = (params) => defHttp.get({ url: Api.queryById, params }, { isTransformResponse: false });
/**
 * Query template list
 * @param params
 */
export const getTempList = (params) => {
  return defHttp.get({ url: Api.tempList, params });
};
