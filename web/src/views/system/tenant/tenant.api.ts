import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';
import { getTenantId } from "/@/utils/auth";

enum Api {
  list = '/sys/tenant/list',
  save = '/sys/tenant/add',
  edit = '/sys/tenant/edit',
  get = '/sys/tenant/queryById',
  delete = '/sys/tenant/delete',
  deleteBatch = '/sys/tenant/deleteBatch',
  getCurrentUserTenants = '/sys/tenant/getCurrentUserTenant',
  invitationUserJoin = '/sys/tenant/invitationUserJoin',
  getTenantUserList = '/sys/tenant/getTenantUserList',
  leaveTenant = '/sys/tenant/leaveTenant',
  packList = '/sys/tenant/packList',
  addPackPermission = '/sys/tenant/addPackPermission',
  editPackPermission = '/sys/tenant/editPackPermission',
  deleteTenantPack = '/sys/tenant/deleteTenantPack',
  recycleBinPageList = '/sys/tenant/recycleBinPageList',
  deleteLogicDeleted = '/sys/tenant/deleteLogicDeleted',
  revertTenantLogic = '/sys/tenant/revertTenantLogic',
  syncDefaultPack = '/sys/tenant/syncDefaultPack',
  //User product package relationshipapi
  queryTenantPackUserList = '/sys/tenant/queryTenantPackUserList',
  deleteTenantPackUser = '/sys/tenant/deleteTenantPackUser',
  addTenantPackUser = '/sys/tenant/addTenantPackUser',
  //Get user tenant list
  getTenantPageListByUserId = '/sys/tenant/getTenantPageListByUserId',
  
  //New、Edit user tenant
  saveUser = '/sys/user/addTenantUser',
  editUser = '/sys/user/editTenantUser',
  //According to tenantidGet the user's product package list and the product packages under the current user with the userid
  listPackByTenantUserId = '/sys/tenant/listPackByTenantUserId',
}

/**
 * Query tenant list
 * @param params
 */
export const getTenantList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * Save or update tenant
 * @param params
 */
export const saveOrUpdateTenant = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};

/**
 * Check tenant details
 * @param params
 */
export const getTenantById = (params) => {
  return defHttp.get({ url: Api.get, params });
};

/**
 * Delete tenant
 * @param params
 */
export const deleteTenant = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * 批量Delete tenant
 * @param params
 */
export const batchDeleteTenant = (params, handleSuccess) => {
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
 * Get logged in user department information
 */
export const getUserTenants = (params?) => defHttp.get({ url: Api.getCurrentUserTenants, params });

/**
 * Invite users to join the tenant
 * @param params
 */
export const invitationUserJoin = (params) => defHttp.put({ url: Api.invitationUserJoin, params }, { joinParamsToUrl: true });

/**
 * by tenantidGet data
 * @param params
 */
export const getTenantUserList = (params) => {
  return defHttp.get({ url: Api.getTenantUserList, params });
};

/**
 * User leaves tenant
 * @param params
 */
export const leaveTenant = (params, handleSuccess) => {
  Modal.confirm({
    title: 'please leave',
    content: '是否将此用户please leave当前租户',
    okText: 'confirm',
    cancelText: 'Cancel',
    onOk: () => {
      return defHttp.put({ url: Api.leaveTenant, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};

/**
 * Get product package list
 * @param params
 */
export const packList = (params) => {
  return defHttp.get({ url: Api.packList, params });
};

/**
 * Add menu
 * @param params
 */
export const addPackPermission = (params) => {
  return defHttp.post({ url: Api.addPackPermission, params });
};

/**
 * Add menu
 * @param params
 */
export const editPackPermission = (params) => {
  return defHttp.put({ url: Api.editPackPermission, params });
};

/**
 * delete menu
 * @param params
 */
export const deleteTenantPack = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteTenantPack, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * Initialize package
 * @param params
 * @param handleSuccess
 */
export const syncDefaultTenantPack = (params, handleSuccess) => {
  return defHttp.post({ url: Api.syncDefaultPack, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * Get a list of tenant recycle bins
 * @param params
 */
export const recycleBinPageList = (params) => {
  return defHttp.get({ url: Api.recycleBinPageList, params });
};

/**
 * Tenant is completely deleted
 * @param params
 */
export const deleteLogicDeleted = (params,handleSuccess) => {
  return defHttp.delete({ url: Api.deleteLogicDeleted, params },{ joinParamsToUrl: true }).then(() => {
    handleSuccess();
  }).catch(()=>{
    handleSuccess();
  });
};

/**
 * Tenant restore
 * @param params
 */
export const revertTenantLogic = (params,handleSuccess) => {
  return defHttp.put({ url: Api.revertTenantLogic, params },{ joinParamsToUrl: true }).then(() => {
    handleSuccess();
  })
};

/**
 * Get the users under the tenant product package
 * @param params
 */
export const queryTenantPackUserList = (params) => {
  return defHttp.get({ url: Api.queryTenantPackUserList, params });
};

/**
 * Remove user and product package relationship data
 * @param params
 */
export const deleteTenantPackUser = (params)=>{
  return defHttp.put({ url: Api.deleteTenantPackUser, params });
}

/**
 * Add relationship data for users and product packages
 * @param params
 */
export const addTenantPackUser = (params)=>{
  return defHttp.post({ url: Api.addTenantPackUser, params });
}

/**
 * Query user tenant list
 * @param params
 */
export const getTenantPageListByUserId = (params) => {
  return defHttp.get({ url: Api.getTenantPageListByUserId, params });
};


/**
 * Get the name of the currently logged in tenant
 */
export async function getLoginTenantName() {
  let tenantId = getTenantId();
  if(tenantId){
    let result = await getTenantById({ id:tenantId });
    if(result){
      return result.name;
    }
  }
  return "null";
}

/**
 * Save or update user
 * @param params
 */
export const saveOrUpdateTenantUser = (params, isUpdate) => {
  let url = isUpdate ? Api.editUser : Api.saveUser;
  return defHttp.post({ url: url, params },{ joinParamsToUrl: true });
};
/**
 * According to tenantidGet the user's product package list and the product packages under the current user with the userid
 * 
 * @param params
 */
export const listPackByTenantUserId = (params) => {
  return defHttp.get({ url: Api.listPackByTenantUserId, params });
}
