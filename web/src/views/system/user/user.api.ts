import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';
import { isObject } from '/@/utils/is';
enum Api {
  listNoCareTenant = '/sys/user/listAll',
  list = '/sys/user/list',
  save = '/sys/user/add',
  edit = '/sys/user/edit',
  getUserRole = '/sys/user/queryUserRole',
  duplicateCheck = '/sys/duplicate/check',
  deleteUser = '/sys/user/delete',
  deleteBatch = '/sys/user/deleteBatch',
  importExcel = '/sys/user/importExcel',
  exportXls = '/sys/user/exportXls',
  recycleBinList = '/sys/user/recycleBin',
  putRecycleBin = '/sys/user/putRecycleBin',
  deleteRecycleBin = '/sys/user/deleteRecycleBin',
  allRolesList = '/sys/role/queryall',
  allRolesListNoByTenant = '/sys/role/queryallNoByTenant',
  allTenantList = '/sys/tenant/queryList',
  allPostList = '/sys/position/list',
  userDepartList = '/sys/user/userDepartList',
  changePassword = '/sys/user/changePassword',
  frozenBatch = '/sys/user/frozenBatch',
  getUserAgent = '/sys/sysUserAgent/queryByUserName',
  userQuitAgent = '/sys/user/userQuitAgent',
  getQuitList = '/sys/user/getQuitList',
  putCancelQuit = '/sys/user/putCancelQuit',
  updateUserTenantStatus='/sys/tenant/updateUserTenantStatus',
  getUserTenantPageList='/sys/tenant/getUserTenantPageList',
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
 * List interface(Query user，Isolation by tenant)
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * List interface(Query all users，不Isolation by tenant)
 * @param params
 */
export const listNoCareTenant = (params) => defHttp.get({ url: Api.listNoCareTenant, params });

/**
 * user role interface
 * @param params
 */
export const getUserRoles = (params) => defHttp.get({ url: Api.getUserRole, params }, { errorMessageMode: 'none' });

/**
 * Delete user
 */
export const deleteUser = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteUser, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * 批量Delete user
 * @param params
 */
export const batchDeleteUser = (params, handleSuccess) => {
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
 * Save or update user
 * @param params
 */
export const saveOrUpdateUser = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};
/**
 * Unique verification
 * @param params
 */
export const duplicateCheck = (params) => defHttp.get({ url: Api.duplicateCheck, params }, { isTransformResponse: false });

/**
 * 20231215
 * liaozhiyang
 * Unique verification（ Delay【Anti-shake】）
 * @param params
 */
const timer = {};
export const duplicateCheckDelay = (params) => {
  return new Promise((resove, rejected) => {
    // -update-begin--author:liaozhiyang---date:20240619---for：【TV360X-1380】Use multipleduplicateCheckDelay，validateWhen the method is called, it will result inpromiseUnable to save due to suspension
    let key;
    if (isObject(params)) {
      key = `${params.tableName}_${params.fieldName}`;
    } else {
      key = params;
    }
    clearTimeout(timer[key]);
    // -update-end--author:liaozhiyang---date:20240619---for：【TV360X-1380】Use multipleduplicateCheckDelay，validateWhen the method is called, it will result inpromiseUnable to save due to suspension
    timer[key] = setTimeout(() => {
      defHttp
        .get({ url: Api.duplicateCheck, params }, { isTransformResponse: false })
        .then((res: any) => {
          resove(res as any);
        })
        .catch((error) => {
          rejected(error);
        });
      delete timer[key];
    }, 500);
  });
};
/**
 * Get all roles（Tenant isolation）
 * @param params
 */
export const getAllRolesList = (params) => defHttp.get({ url: Api.allRolesList, params });
/**
 * Get all roles（不Tenant isolation）
 * @param params
 */
export const getAllRolesListNoByTenant = (params) => defHttp.get({ url: Api.allRolesListNoByTenant, params });
/**
 * Get all tenants
 */
export const getAllTenantList = (params) => defHttp.get({ url: Api.allTenantList, params });
/**
 * Get the designated user responsible department
 */
export const getUserDepartList = (params) => defHttp.get({ url: Api.userDepartList, params }, { successMessageMode: 'none' });
/**
 * Get all jobs
 */
export const getAllPostList = (params) => {
  return new Promise((resolve) => {
    defHttp.get({ url: Api.allPostList, params }).then((res) => {
      resolve(res.records);
    });
  });
};
/**
 * Recycle bin list
 * @param params
 */
export const getRecycleBinList = (params) => defHttp.get({ url: Api.recycleBinList, params });
/**
 * Recycle Bin Restore
 * @param params
 */
export const putRecycleBin = (params, handleSuccess) => {
  return defHttp.put({ url: Api.putRecycleBin, params }).then(() => {
    handleSuccess();
  });
};
/**
 * Recycle bin delete
 * @param params
 */
export const deleteRecycleBin = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteRecycleBin, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * Change password
 * @param params
 */
export const changePassword = (params) => {
  return defHttp.put({ url: Api.changePassword, params });
};
/**
 * Freeze and Thaw
 * @param params
 */
export const frozenBatch = (params, handleSuccess) => {
  return defHttp.put({ url: Api.frozenBatch, params }).then(() => {
    handleSuccess();
  });
};


/**
 * User resignation list
 * @param params
 */
export const getQuitList = (params) => {
  return defHttp.get({ url: Api.getQuitList, params });
};

/**
 * Cancel离职
 * @param params
 */
export const putCancelQuit = (params, handleSuccess) => {
  return defHttp.put({ url: Api.putCancelQuit, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * Get list data pending approval
 */
export const getUserTenantPageList = (params) => {
  return defHttp.get({ url: Api.getUserTenantPageList, params });
};

/**
 * Update tenant status
 * @param params
 */
export const updateUserTenantStatus = (params) => {
  return defHttp.put({ url: Api.updateUserTenantStatus, params }, { joinParamsToUrl: true, isTransformResponse: false });
};
