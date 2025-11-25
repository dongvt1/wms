import { unref } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  treeList = '/sys/sysDepart/queryMyDeptTreeList',
  queryIdTree = '/sys/sysDepart/queryIdTree',
  searchBy = '/sys/sysDepart/searchBy',
}

// Department usersAPI
enum DepartUserApi {
  list = '/sys/user/departUserList',
  link = '/sys/user/editSysDepartWithUser',
  unlink = '/sys/user/deleteUserInDepartBatch',
}

// Department roleAPI
enum DepartRoleApi {
  list = '/sys/sysDepartRole/list',
  deleteBatch = '/sys/sysDepartRole/deleteBatch',
  save = '/sys/sysDepartRole/add',
  edit = '/sys/sysDepartRole/edit',
  queryTreeListForDeptRole = '/sys/sysDepartPermission/queryTreeListForDeptRole',
  queryDeptRolePermission = '/sys/sysDepartPermission/queryDeptRolePermission',
  saveDeptRolePermission = '/sys/sysDepartPermission/saveDeptRolePermission',
  dataRule = '/sys/sysDepartRole/datarule',
  getDeptRoleList = '/sys/sysDepartRole/getDeptRoleList',
  getDeptRoleByUserId = '/sys/sysDepartRole/getDeptRoleByUserId',
  saveDeptRoleUser = '/sys/sysDepartRole/deptRoleUserAdd',
}

/**
 * Get department tree list
 */
export const queryMyDepartTreeList = (params?) => defHttp.get({ url: Api.treeList, params }, { isTransformResponse: false });

/**
 * Query data，Load the names of all departments in a tree structure
 */
export const queryIdTree = (params?) => defHttp.get({ url: Api.queryIdTree, params });

/**
 * Search departments based on keywords
 */
export const searchByKeywords = (params) => defHttp.get({ url: Api.searchBy, params });

/**
 * Query user information under a department
 */
export const departUserList = (params) => defHttp.get({ url: DepartUserApi.list, params });

/**
 * Add relationships between departments and users in batches
 *
 * @param departId departmentID
 * @param userIdList userIDlist
 */
export const linkDepartUserBatch = (departId: string, userIdList: string[]) =>
  defHttp.post({ url: DepartUserApi.link, params: { depId: departId, userIdList } });

/**
 * 批量取消department和user的关联关系
 */
export const unlinkDepartUserBatch = (params, confirm = false) => {
  return new Promise((resolve, reject) => {
    const doDelete = () => {
      resolve(defHttp.delete({ url: DepartUserApi.unlink, params }, { joinParamsToUrl: true }));
    };
    if (confirm) {
      createConfirm({
        iconType: 'warning',
        title: 'Disassociate',
        content: '确定要Disassociate吗？',
        onOk: () => doDelete(),
        onCancel: () => reject(),
      });
    } else {
      doDelete();
    }
  });
};

/**
 * 查询Department role信息
 */
export const departRoleList = (params) => defHttp.get({ url: DepartRoleApi.list, params });

/**
 * 保存或者更新Department role
 */
export const saveOrUpdateDepartRole = (params, isUpdate) => {
  if (isUpdate) {
    return defHttp.put({ url: DepartRoleApi.edit, params });
  } else {
    return defHttp.post({ url: DepartRoleApi.save, params });
  }
};

/**
 * 批量deleteDepartment role
 */
export const deleteBatchDepartRole = (params, confirm = false) => {
  return new Promise((resolve, reject) => {
    const doDelete = () => {
      resolve(defHttp.delete({ url: DepartRoleApi.deleteBatch, params }, { joinParamsToUrl: true }));
    };
    if (confirm) {
      createConfirm({
        iconType: 'warning',
        title: 'delete',
        content: '确定要delete吗？',
        onOk: () => doDelete(),
        onCancel: () => reject(),
      });
    } else {
      doDelete();
    }
  });
};

/**
 * user角色授权功能，Query menu permission tree
 */
export const queryTreeListForDeptRole = (params) => defHttp.get({ url: DepartRoleApi.queryTreeListForDeptRole, params });
/**
 * Query role authorization
 */
export const queryDeptRolePermission = (params) => defHttp.get({ url: DepartRoleApi.queryDeptRolePermission, params });
/**
 * Save role authorization
 */
export const saveDeptRolePermission = (params) => defHttp.post({ url: DepartRoleApi.saveDeptRolePermission, params });

/**
 *  查询Department role数据权限list
 */
export const queryDepartRoleDataRule = (functionId, departId, roleId, params?) => {
  let url = `${DepartRoleApi.dataRule}/${unref(functionId)}/${unref(departId)}/${unref(roleId)}`;
  return defHttp.get({ url, params });
};
/**
 * 保存Department role数据权限
 */
export const saveDepartRoleDataRule = (params) => defHttp.post({ url: DepartRoleApi.dataRule, params });
/**
 * 查询Department roleuser授权
 */
export const queryDepartRoleUserList = (params) => defHttp.get({ url: DepartRoleApi.getDeptRoleList, params });
/**
 * according to userId 查询Department roleuser授权
 */
export const queryDepartRoleByUserId = (params) => defHttp.get({ url: DepartRoleApi.getDeptRoleByUserId, params });
/**
 * 保存Department roleuser授权
 */
export const saveDepartRoleUser = (params) => defHttp.post({ url: DepartRoleApi.saveDeptRoleUser, params });
