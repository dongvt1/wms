import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/airag/airagModel/list',
  save = '/airag/airagModel/add',
  testConn = '/airag/airagModel/test',
  delete = '/airag/airagModel/delete',
  queryById = '/airag/airagModel/queryById',
  edit = '/airag/airagModel/edit',
}

/**
 * Query AI model
 * @param params
 */
export const list = (params) => {
  return defHttp.get({ url: Api.list, params }, { isTransformResponse: false });
};

/**
 * Query AI model by ID
 * @param params
 */
export const queryById = (params) => {
  return defHttp.get({ url: Api.queryById, params }, { isTransformResponse: false });
};

/**
 * Add AI model
 *
 * @param params
 */
export const saveModel = (params) => {
  return defHttp.post({ url: Api.save, params });
};

/**
 * Edit AI model
 *
 * @param params
 */
export const editModel = (params) => {
  return defHttp.put({ url: Api.edit, params });
};

/**
 * Test connection
 *
 * @param params
 */
export const testConn = (params) => {
  return defHttp.post({ url: Api.testConn, params });
};

/**
 * Delete model
 */
export const deleteModel = (params, handleSuccess) => {
  Modal.confirm({
    title: 'Confirm Delete',
    content: 'Are you sure you want to delete the model named ' + params.name + '?',
    okText: 'Confirm',
    cancelText: 'Cancel',
    onOk: () => {
      return defHttp.delete({ url: Api.delete, params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};
