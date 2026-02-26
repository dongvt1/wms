import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/openapi/auth/list',
  save='/openapi/auth/add',
  edit='/openapi/auth/edit',
  apiList= '/openapi/list',
  genAKSK = '/openapi/auth/genAKSK',
  permissionList='/openapi/permission/getOpenApi',
  permissionAdd='/openapi/permission/add',
  deleteOne = '/openapi/auth/delete',
  deleteBatch = '/openapi/auth/deleteBatch',
  importExcel = '/openapi/auth/importExcel',
  exportXls = '/openapi/auth/exportXls',
}

/**
 * GetAPI
 * @param params
 */
export const apiList = Api.apiList;
/**
 * Permission added
 * @param params
 */
export const permissionAdd = Api.permissionAdd;
/**
 * generateAKSK
 * @param params
 */
export const genAKSK = Api.genAKSK;

/**
 * Exportapi
 * @param params
 */
export const getExportUrl = Api.exportXls;

/**
 * importapi
 */
export const getImportUrl = Api.importExcel;

/**
 * List interface
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * Delete a single
 * @param params
 * @param handleSuccess
 */
export const deleteOne = (params,handleSuccess) => {
  return defHttp.delete({url: Api.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}

/**
 * Batch delete
 * @param params
 * @param handleSuccess
 */
export const batchDelete = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: 'Confirm deletion',
    content: 'Whether to delete selected data',
    okText: 'confirm',
    cancelText: 'Cancel',
    onOk: () => {
      return defHttp.delete({url: Api.deleteBatch, data: params}, {joinParamsToUrl: true}).then(() => {
        handleSuccess();
      });
    }
  });
}

/**
 * Save or update
 * @param params
 * @param isUpdate
 */
export const saveOrUpdate = (params, isUpdate) => {
  if (isUpdate) {
    return defHttp.put({ url: Api.edit, params }, { isTransformResponse: false });
  }
  return defHttp.post({ url: Api.save, params }, { isTransformResponse: false });
}

/**
 * 全部权限List interface
 * @param params
 */
export const getApiList = (params) => defHttp.get({ url: Api.apiList, params }, { isTransformResponse: false });

/**
 * Get已授权项目的接口
 * @param params
 */
export const getPermissionList = (params) => defHttp.get({ url: Api.permissionList, params });
/**
 * Authorization saving method
 * @param params
 * @param isUpdate
 */
export const permissionAddFunction = (params) => {
  return defHttp.post({ url: Api.permissionAdd, params }, { isTransformResponse: false });
}
/**
 * Authorization saving method
 * @param params
 * @param isUpdate
 */
export const getGenAKSK = (params) => {
  return defHttp.get({ url: Api.genAKSK, params });
}
