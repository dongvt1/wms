import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';
enum Api {
  list = '/sys/dict/list',
  save = '/sys/dict/add',
  edit = '/sys/dict/edit',
  duplicateCheck = '/sys/duplicate/check',
  deleteDict = '/sys/dict/delete',
  deleteBatch = '/sys/dict/deleteBatch',
  importExcel = '/sys/dict/importExcel',
  exportXls = '/sys/dict/exportXls',
  recycleBinList = '/sys/dict/deleteList',
  putRecycleBin = '/sys/dict/back',
  batchPutRecycleBin = '/sys/dict/putRecycleBin',
  batchDeleteRecycleBin = '/sys/dict/deleteRecycleBin',
  deleteRecycleBin = '/sys/dict/deletePhysic',
  itemList = '/sys/dictItem/list',
  deleteItem = '/sys/dictItem/delete',
  itemSave = '/sys/dictItem/add',
  itemEdit = '/sys/dictItem/edit',
  dictItemCheck = '/sys/dictItem/dictItemCheck',
  refreshCache = '/sys/dict/refleshCache',
  queryAllDictItems = '/sys/dict/queryAllDictItems',
}
/**
 * Exportapi
 * @param params
 */
export const getExportUrl = Api.exportXls;
/**
 * importapi
 * @param params
 */
export const getImportUrl = Api.importExcel;
/**
 * dictionary list interface
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });
/**
 * delete dictionary
 */
export const deleteDict = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteDict, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * 批量delete dictionary
 * @param params
 */
export const batchDeleteDict = (params, handleSuccess) => {
  Modal.confirm({
    title: 'Confirm deletion',
    content: 'Whether to delete selected data',
    okText: 'confirm',
    cancelText: 'Cancel',
    onOk: () => {
      return defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};
/**
 * Save or update dictionary
 * @param params
 */
export const saveOrUpdateDict = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};
/**
 * Unique verification
 * @param params
 */
export const duplicateCheck = (params) => defHttp.get({ url: Api.duplicateCheck, params }, { isTransformResponse: false });
/**
 * Dictionary recycle bin list
 * @param params
 */
export const getRecycleBinList = (params) => defHttp.get({ url: Api.recycleBinList, params });

/**
 * Recycle Bin batch restore
 * @param params
 */
export const batchPutRecycleBin = (params, handleSuccess) => {
  return defHttp.put({ url: Api.batchPutRecycleBin, params}).then(() => {
    handleSuccess();
  });
};
/**
 * Recycle Bin Restore
 * @param params
 */
export const putRecycleBin = (id, handleSuccess) => {
  return defHttp.put({ url: Api.putRecycleBin + `/${id}` }).then(() => {
    handleSuccess();
  });
};
/**
 * Recycle bin batch deletion
 * @param params
 */
export const batchDeleteRecycleBin = (params, handleSuccess) => {
  return defHttp.delete({ url: `${Api.batchDeleteRecycleBin}?ids=${params.ids}`}).then(() => {
    handleSuccess();
  });
};
/**
 * Recycle bin delete
 * @param params
 */
export const deleteRecycleBin = (id, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteRecycleBin + `/${id}` }).then(() => {
    handleSuccess();
  });
};
/**
 * Dictionary configuration list
 * @param params
 */
export const itemList = (params) => defHttp.get({ url: Api.itemList, params });
/**
 * Dictionary configuration deletion
 * @param params
 */
export const deleteItem = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteItem, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * Save or update dictionary配置
 * @param params
 */
export const saveOrUpdateDictItem = (params, isUpdate) => {
  let url = isUpdate ? Api.itemEdit : Api.itemSave;
  return defHttp.post({ url: url, params });
};
/**
 * Verify dictionary data values
 * @param params
 */
export const dictItemCheck = (params) => defHttp.get({ url: Api.dictItemCheck, params }, { isTransformResponse: false });
/**
 * refresh dictionary
 * @param params
 */
export const refreshCache = () => defHttp.get({ url: Api.refreshCache }, { isTransformResponse: false });
/**
 * Get all dictionary items
 * @param params
 */
export const queryAllDictItems = () => defHttp.get({ url: Api.queryAllDictItems }, { isTransformResponse: false });
