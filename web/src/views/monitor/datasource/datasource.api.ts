import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/sys/dataSource/list',
  save = '/sys/dataSource/add',
  edit = '/sys/dataSource/edit',
  get = '/sys/dataSource/queryById',
  delete = '/sys/dataSource/delete',
  testConnection = '/online/cgreport/api/testConnection',
  deleteBatch = '/sys/dataSource/deleteBatch',
  exportXlsUrl = 'sys/dataSource/exportXls',
  importExcelUrl = 'sys/dataSource/importExcel',
}
/**
 * Exportapi
 */
export const getExportUrl = Api.exportXlsUrl;
/**
 * importapi
 */
export const getImportUrl = Api.importExcelUrl;

/**
 * Query data source list
 * @param params
 */
export const getDataSourceList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * Save or update data source
 * @param params
 */
export const saveOrUpdateDataSource = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};

/**
 * Query data source details
 * @param params
 */
export const getDataSourceById = (params) => {
  return defHttp.get({ url: Api.get, params });
};

/**
 * Delete data source
 * @param params
 */
export const deleteDataSource = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * test connection
 * @param params
 */
export const testConnection = (params) => {
  return defHttp.post({ url: Api.testConnection, params });
};

/**
 * 批量Delete data source
 * @param params
 */
export const batchDeleteDataSource = (params, handleSuccess) => {
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
