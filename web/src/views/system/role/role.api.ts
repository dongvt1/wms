import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/sys/role/list',
  listByTenant = '/sys/role/listByTenant',
  save = '/sys/role/add',
  edit = '/sys/role/edit',
  deleteRole = '/sys/role/delete',
  deleteBatch = '/sys/role/deleteBatch',
  exportXls = '/sys/role/exportXls',
  importExcel = '/sys/role/importExcel',
  isRoleExist = '/sys/role/checkRoleCode',
  queryTreeListForRole = '/sys/role/queryTreeList',
  queryRolePermission = '/sys/permission/queryRolePermission',
  saveRolePermission = '/sys/permission/saveRolePermission',
  queryDataRule = '/sys/role/datarule',
  getParentDesignList = '/act/process/extActDesignFlowData/getDesFormFlows',
  getRoleDegisnList = '/joa/designform/designFormCommuse/getRoleDegisnList',
  saveRoleDesign = '/joa/designform/designFormCommuse/sysRoleDesignAdd',
  userList = '/sys/user/userRoleList',
  deleteUserRole = '/sys/user/deleteUserRole',
  batchDeleteUserRole = '/sys/user/deleteUserRoleBatch',
  addUserRole = '/sys/user/addSysUserRole',
  saveRoleIndex = '/sys/sysRoleIndex/add',
  editRoleIndex = '/sys/sysRoleIndex/edit',
  queryIndexByCode = '/sys/sysRoleIndex/queryByCode',
}
/**
 * Exportapi
 */
export const getExportUrl = Api.exportXls;
/**
 * importapi
 */
export const getImportUrl = Api.importExcel;
/**
 * System role list
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });
/**
 * Tenant role list
 * @param params
 */
export const listByTenant = (params) => defHttp.get({ url: Api.listByTenant, params });

/**
 * Delete role
 */
export const deleteRole = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteRole, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * 批量Delete role
 * @param params
 */
export const batchDeleteRole = (params, handleSuccess) => {
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
 * Save or update role
 * @param params
 */
export const saveOrUpdateRole = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};
/**
 * Coding verification
 * @param params
 */
// update-begin--author:liaozhiyang---date:20231215---for：【QQYUN-7415】Add anti-shake to the form calling interface for verification
let timer;
export const isRoleExist = (params) => {
  return new Promise((resolve, rejected) => {
    clearTimeout(timer);
    timer = setTimeout(() => {
      defHttp
        .get({ url: Api.isRoleExist, params }, { isTransformResponse: false })
        .then((res) => {
          resolve(res);
        })
        .catch((error) => {
          rejected(error);
        });
    }, 500);
  });
};
// update-end--author:liaozhiyang---date:20231215---for：【QQYUN-7415】Add anti-shake to the form calling interface for verification
/**
 * Query tree information based on role
 */
export const queryTreeListForRole = () => defHttp.get({ url: Api.queryTreeListForRole });
/**
 * Query role permissions
 */
export const queryRolePermission = (params) => defHttp.get({ url: Api.queryRolePermission, params });
/**
 * Save role permissions
 */
export const saveRolePermission = (params) => defHttp.post({ url: Api.saveRolePermission, params });
/**
 * Query role data rules
 */
export const queryDataRule = (params) =>
  defHttp.get({ url: `${Api.queryDataRule}/${params.functionId}/${params.roleId}` }, { isTransformResponse: false });
/**
 * Save role data rules
 */
export const saveDataRule = (params) => defHttp.post({ url: Api.queryDataRule, params });
/**
 * Get form data
 * @return List<Map>
 */
export const getParentDesignList = () => defHttp.get({ url: Api.getParentDesignList });
/**
 * Get role form data
 * @return List<Map>
 */
export const getRoleDegisnList = (params) => defHttp.get({ url: Api.getRoleDegisnList, params });
/**
 * Submit role ticket information
 */
export const saveRoleDesign = (params) => defHttp.post({ url: Api.saveRoleDesign, params });
/**
 * Role list interface
 * @param params
 */
export const userList = (params) => defHttp.get({ url: Api.userList, params });
/**
 * Delete role用户
 */
export const deleteUserRole = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteUserRole, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * 批量Delete role用户
 * @param params
 */
export const batchDeleteUserRole = (params, handleSuccess) => {
  Modal.confirm({
    title: 'Confirm deletion',
    content: 'Whether to delete selected data',
    okText: 'confirm',
    cancelText: 'Cancel',
    onOk: () => {
      return defHttp.delete({ url: Api.batchDeleteUserRole, params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};
/**
 * Add existing user
 */
export const addUserRole = (params, handleSuccess) => {
  return defHttp.post({ url: Api.addUserRole, params }).then(() => {
    handleSuccess();
  });
};
/**
 * Save or update
 * @param params
 * @param isUpdate Is it updating data?
 */
export const saveOrUpdateRoleIndex = (params, isUpdate) => {
  let url = isUpdate ? Api.editRoleIndex : Api.saveRoleIndex;
  return defHttp.post({ url: url, params });
};
/**
 * according tocodeQuery home page configuration
 * @param params
 */
export const queryIndexByCode = (params) => defHttp.get({ url: Api.queryIndexByCode, params }, { isTransformResponse: false });
