import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/sys/sysRoleIndex/list',
  save = '/sys/sysRoleIndex/add',
  edit = '/sys/sysRoleIndex/edit',
  deleteIndex = '/sys/sysRoleIndex/delete',
  deleteBatch = '/sys/sysRoleIndex/deleteBatch',
  queryIndexByCode = '/sys/sysRoleIndex/queryByCode',
}
/**
 * System role list
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * Delete role
 */
export const deleteIndex = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteIndex, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * 批量Delete role
 * @param params
 */
export const batchDelete = (params, handleSuccess) => {
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
 * Save or update homepage configuration
 * @param params
 */
export const saveOrUpdate = (params, isUpdate) => {
  const url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};
/**
 * Query home page configuration
 * @param params
 */
export const queryIndexByCode = (params) => defHttp.get({ url: Api.queryIndexByCode, params }, { isTransformResponse: false });
