import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/openapi/list',
  save='/openapi/add',
  edit='/openapi/edit',
  deleteOne = '/openapi/delete',
  deleteBatch = '/openapi/deleteBatch',
  genPath = '/openapi/genPath',
  importExcel = '/openapi/importExcel',
  exportXls = '/openapi/exportXls',
  openApiHeaderList = '/openapi/list',
  openApiParamList = '/openapi/list',
  openApiJson = '/openapi/json',
}

/**
 * Subform query interface
 * @param params
 */
export const genPath = Api.genPath
/**
 * swaggerdocumentjson
 * @param params
 */
export const openApiJson = Api.openApiJson
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
 * Subform query interface
 * @param params
 */
export const queryOpenApiHeader = Api.openApiHeaderList
/**
 * Subform query interface
 * @param params
 */
export const queryOpenApiParam = Api.openApiParamList

/**
 * List interface
 * @param params
 */
export const list = (params) =>
  defHttp.get({url: Api.list, params});

/**
 * Delete a single
 */
export const deleteOne = (params,handleSuccess) => {
  return defHttp.delete({url: Api.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}
/**
 * Batch delete
 * @param params
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
 */
export const saveOrUpdate = (params, isUpdate) => {
  if (isUpdate) {
    return defHttp.put({url: Api.edit, params});
  } else {
    return defHttp.post({url: Api.save, params});
  }
}
/**
 * Get interface address
 * @param params
 */
export const getGenPath = (params) =>
  defHttp.get({url: Api.genPath, params},{isTransformResponse:false});
/**
 * 子表List interface
 * @param params
 */
export const openApiHeaderList = (params) =>
  defHttp.get({url: Api.openApiHeaderList, params},{isTransformResponse:false});
/**
 * 子表List interface
 * @param params
 */
export const openApiParamList = (params) =>
  defHttp.get({url: Api.openApiParamList, params},{isTransformResponse:false});
/**
 * swaggerdocumentjson
 * @param params
 */
export const getOpenApiJson = (params) =>
  defHttp.get({url: Api.openApiJson, params},{isTransformResponse:false});
