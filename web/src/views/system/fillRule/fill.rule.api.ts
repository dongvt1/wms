import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/sys/fillRule/list',
  test = '/sys/fillRule/testFillRule',
  save = '/sys/fillRule/add',
  edit = '/sys/fillRule/edit',
  delete = '/sys/fillRule/delete',
  deleteBatch = '/sys/fillRule/deleteBatch',
  exportXls = '/sys/fillRule/exportXls',
  importExcel = '/sys/fillRule/importExcel',
}

/**
 * export address
 */
export const exportUrl = Api.exportXls;
/**
 * Import address
 */
export const importUrl = Api.importExcel;

/**
 * List query
 * @param params
 */
export const getFillRuleList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * delete
 * @param params
 * @param handleSuccess
 */
export const deleteFillRule = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * 批量delete
 * @param params
 */
export const batchDeleteFillRule = (params, handleSuccess) => {
  Modal.confirm({
    title: 'confirmdelete',
    content: '是否delete选中数据',
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
 * Rule function test
 * @param params
 */
export const handleTest = (params) => {
  return defHttp.get({ url: Api.test, params }, { isTransformResponse: false });
};

/**
 * save
 * @param params
 */
export const saveFillRule = (params) => {
  return defHttp.post({ url: Api.save, params });
};

/**
 * renew
 * @param params
 */
export const updateFillRule = (params) => {
  return defHttp.put({ url: Api.edit, params });
};
