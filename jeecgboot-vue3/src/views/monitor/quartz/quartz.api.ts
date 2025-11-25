import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/sys/quartzJob/list',
  save = '/sys/quartzJob/add',
  edit = '/sys/quartzJob/edit',
  get = '/sys/quartzJob/queryById',
  pause = '/sys/quartzJob/pause',
  resume = '/sys/quartzJob/resume',
  delete = '/sys/quartzJob/delete',
  exportXlsUrl = '/sys/quartzJob/exportXls',
  importExcelUrl = '/sys/quartzJob/importExcel',
  execute = '/sys/quartzJob/execute',
  deleteBatch = '/sys/quartzJob/deleteBatch',
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
 * Query task list
 * @param params
 */
export const getQuartzList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * Save or update tasks
 * @param params
 */
export const saveOrUpdateQuartz = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};

/**
 * Query task details
 * @param params
 */
export const getQuartzById = (params) => {
  return defHttp.get({ url: Api.get, params });
};

/**
 * Delete task
 * @param params
 */
export const deleteQuartz = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * start up
 * @param params
 */
export const resumeJob = (params, handleSuccess) => {
  return defHttp.get({ url: Api.resume, params }).then(() => {
    handleSuccess();
  });
};

/**
 * pause
 * @param params
 */
export const pauseJob = (params, handleSuccess) => {
  return defHttp.get({ url: Api.pause, params }).then(() => {
    handleSuccess();
  });
};

/**
 * Execute immediately
 * @param params
 */
export const executeImmediately = (params, handleSuccess) => {
  return defHttp.get({ url: Api.execute, params }).then(() => {
    handleSuccess();
  });
};

/**
 * 批量Delete task
 * @param params
 */
export const batchDeleteQuartz = (params, handleSuccess) => {
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
