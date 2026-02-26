import { unref } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

export enum Api {
  queryDepartTreeSync = '/sys/sysDepart/queryDepartTreeSync',
  save = '/sys/sysDepart/add',
  edit = '/sys/sysDepart/edit',
  delete = '/sys/sysDepart/delete',
  deleteBatch = '/sys/sysDepart/deleteBatch',
  exportXlsUrl = '/sys/sysDepart/exportXls',
  importExcelUrl = '/sys/sysDepart/importExcel',

  roleQueryTreeList = '/sys/role/queryTreeList',
  queryDepartPermission = '/sys/permission/queryDepartPermission',
  saveDepartPermission = '/sys/permission/saveDepartPermission',

  dataRule = '/sys/sysDepartPermission/datarule',

  getCurrentUserDeparts = '/sys/user/getCurrentUserDeparts',
  selectDepart = '/sys/selectDepart',
  getUpdateDepartInfo = '/sys/user/getUpdateDepartInfo',
  doUpdateDepartInfo = '/sys/user/doUpdateDepartInfo',
  changeDepartChargePerson = '/sys/user/changeDepartChargePerson',
  //According to departmentidGet job information
  getPositionByDepartId = '/sys/sysDepart/getPositionByDepartId',
  //According to departmentidObtain the relationship between superiors and subordinates of the position
  getRankRelation = '/sys/sysDepart/getRankRelation',
  //Asynchronously obtain departments and positions
  queryDepartAndPostTreeSync = '/sys/sysDepart/queryDepartAndPostTreeSync',
  //Get members under department and position
  queryByOrgCodeForAddressList = '/sys/user/queryByOrgCodeForAddressList',
}

/**
 * Get department tree list
 */
export const queryDepartTreeSync = (params?) => defHttp.get({ url: Api.queryDepartTreeSync, params });

/**
 * Get a list of departments and job trees
 */
export const queryDepartAndPostTreeSync = (params?) => defHttp.get({ url: Api.queryDepartAndPostTreeSync, params });

/**
 * Save or update department roles
 */
export const saveOrUpdateDepart = (params, isUpdate) => {
  if (isUpdate) {
    return defHttp.put({ url: Api.edit, params });
  } else {
    return defHttp.post({ url: Api.save, params });
  }
};

/**
 * Delete department roles in batches
 */
export const deleteBatchDepart = (params, confirm = false) => {
  return new Promise((resolve, reject) => {
    const doDelete = () => {
      resolve(defHttp.delete({ url: Api.deleteBatch, params }, { joinParamsToUrl: true }));
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
 * Get the permission tree list
 */
export const queryRoleTreeList = (params?) => defHttp.get({ url: Api.roleQueryTreeList, params });
/**
 * Query department permissions
 */
export const queryDepartPermission = (params?) => defHttp.get({ url: Api.queryDepartPermission, params });
/**
 * Save department permissions
 */
export const saveDepartPermission = (params) => defHttp.post({ url: Api.saveDepartPermission, params });

/**
 *  Query department data permission list
 */
export const queryDepartDataRule = (functionId, departId, params?) => {
  let url = `${Api.dataRule}/${unref(functionId)}/${unref(departId)}`;
  return defHttp.get({ url, params });
};
/**
 * Save department data permissions
 */
export const saveDepartDataRule = (params) => defHttp.post({ url: Api.dataRule, params });
/**
 * Get logged in user department information
 */
export const getUserDeparts = (params?) => defHttp.get({ url: Api.getCurrentUserDeparts, params });
/**
 * Switch to select department
 */
export const selectDepart = (params?) => defHttp.put({ url: Api.selectDepart, params });

/**
 * Obtain department-related information before editing a department
 * @param id
 */
export const getUpdateDepartInfo = (id) => defHttp.get({ url: Api.getUpdateDepartInfo, params: {id} });

/**
 * Editorial Department
 * @param params
 */
export const doUpdateDepartInfo = (params) => defHttp.put({ url: Api.doUpdateDepartInfo, params });

/**
 * delete部门
 * @param id
 */
export const deleteDepart = (id) => defHttp.delete({ url: Api.delete, params:{ id } }, { joinParamsToUrl: true });

/**
 * Set up a person in charge Cancel the person in charge
 * @param params
 */
export const changeDepartChargePerson = (params) => defHttp.put({ url: Api.changeDepartChargePerson, params });

/**
 * According to departmentidGet job information
 */
export const getPositionByDepartId = (params) => defHttp.get({ url: Api.getPositionByDepartId, params }, { isTransformResponse: false });

/**
 * According to departmentidObtain the relationship between superiors and subordinates of the position
 * @param params
 */
export const getRankRelation = (params) => defHttp.get({ url: Api.getRankRelation, params }, { isTransformResponse: false });

/**
 * According to department或岗位编码获取通讯录成员
 * 
 * @param params
 */
export const queryByOrgCodeForAddressList = (params) => defHttp.get({ url: Api.queryByOrgCodeForAddressList, params });