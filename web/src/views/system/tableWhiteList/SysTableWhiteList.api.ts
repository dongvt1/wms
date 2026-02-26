import {defHttp} from '/@/utils/http/axios';
import {Modal} from 'ant-design-vue';

enum Api {
  list = '/sys/tableWhiteList/list',
  save = '/sys/tableWhiteList/add',
  edit = '/sys/tableWhiteList/edit',
  deleteOne = '/sys/tableWhiteList/delete',
  deleteBatch = '/sys/tableWhiteList/deleteBatch',
  importExcel = '/sys/tableWhiteList/importExcel',
  exportXls = '/sys/tableWhiteList/exportXls',
}

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
export const list = (params) =>
  defHttp.get({url: Api.list, params});

/**
 * Delete a single
 * @param params
 * @param handleSuccess
 */
export const deleteOne = (params, handleSuccess) => {
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
  Modal.confirm({
    title: 'Confirm deletion',
    content: 'Whether to delete selected data',
    okText: 'confirm',
    cancelText: 'Cancel',
    onOk: () => {
      return defHttp.delete({
        url: Api.deleteBatch,
        data: params
      }, {joinParamsToUrl: true}).then(() => {
        handleSuccess();
      });
    }
  });
}
/**
 * Save or update
 * @param params
 * @param isUpdate Is it updating data?
 */
export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({url: url, params});
}
