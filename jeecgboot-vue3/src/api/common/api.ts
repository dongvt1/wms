import { defHttp } from '/@/utils/http/axios';
import { message } from 'ant-design-vue';
import { useGlobSetting } from '/@/hooks/setting';
const globSetting = useGlobSetting();
const baseUploadUrl = globSetting.uploadUrl;
enum Api {
  positionList = '/sys/position/list',
  userList = '/sys/user/list',
  roleList = '/sys/role/list',
  queryDepartTreeSync = '/sys/sysDepart/queryDepartTreeSync',
  queryTreeList = '/sys/sysDepart/queryTreeList',
  loadTreeData = '/sys/category/loadTreeData',
  loadDictItem = '/sys/category/loadDictItem/',
  getDictItems = '/sys/dict/getDictItems/',
  getTableList = '/sys/user/queryUserComponentData',
  getCategoryData = '/sys/category/loadAllData',
  refreshDragCache = '/drag/page/refreshCache',
  refreshDefaultIndexCache = '/sys/sysRoleIndex/cleanDefaultIndexCache',
  //Asynchronously obtain departments and positions
  queryDepartAndPostTreeSync = '/sys/sysDepart/queryDepartAndPostTreeSync',
  //Query users under department positions
  queryDepartPostUserPageList = '/sys/user/queryDepartPostUserPageList',
  //Query all parent nodes of the selected departmentID
  queryAllParentId = '/sys/sysDepart/queryAllParentId',
}

/**
 * Upload parent path
 */
export const uploadUrl = `${baseUploadUrl}/sys/common/upload`;

/**
 * job list
 * @param params
 */
export const getPositionList = (params) => {
  return defHttp.get({ url: Api.positionList, params });
};

/**
 * User list
 * @param params
 */
export const getUserList = (params) => {
  return defHttp.get({ url: Api.userList, params });
};

/**
 * role list
 * @param params
 */
export const getRoleList = (params) => {
  return defHttp.get({ url: Api.roleList, params });
};

/**
 * Asynchronously obtain the department tree list
 */
export const queryDepartTreeSync = (params?) => {
  return defHttp.get({ url: Api.queryDepartTreeSync, params });
};
/**
 * Asynchronously obtain the department position tree list
 */
export const queryDepartAndPostTreeSync = (params?) => {
  return defHttp.get({ url: Api.queryDepartAndPostTreeSync, params });
};

/**
 * Get department tree list
 */
export const queryTreeList = (params?) => {
  return defHttp.get({ url: Api.queryTreeList, params });
};

/**
 * Classification Dictionary Tree Control Load node
 */
export const loadTreeData = (params?) => {
  return defHttp.get({ url: Api.loadTreeData, params });
};

/**
 * according to dictionarycodeLoad dictionarytext
 */
export const loadDictItem = (params?) => {
  return defHttp.get({ url: Api.loadDictItem, params });
};

/**
 * according to dictionarycodeLoad dictionarytext
 */
export const getDictItems = (dictCode) => {
  return defHttp.get({ url: Api.getDictItems + dictCode }, { joinTime: false });
};
/**
 * Department usersmodalSelect list loadinglist
 */
export const getTableList = (params) => {
  return defHttp.get({ url: Api.getTableList, params });
};

/**
 * Department position usermodal【Query users under department positions】
 */
export const queryDepartPostUserPageList = (params) => {
  return defHttp.get({ url: Api.queryDepartPostUserPageList, params });
};

/**
 * Query all parent nodes of the selected departmentID
 */
export const queryAllParentId = (params) => {
  return defHttp.get({ url: Api.queryAllParentId, params });
};

/**
 * Load all classification dictionary data
 */
export const loadCategoryData = (params) => {
  return defHttp.get({ url: Api.getCategoryData, params });
};
/**
 * File upload
 */
export const uploadFile = (params, success) => {
  return defHttp.uploadFile({ url: uploadUrl }, params, { success });
};
/**
 * Download file
 * @param url file path
 * @param fileName file name
 * @param parameter
 * @returns {*}
 */
export const downloadFile = (url, fileName?, parameter?) => {
  return getFileblob(url, parameter).then((data) => {
    if (!data || data.size === 0) {
      message.warning('File download failed');
      return;
    }
    if (typeof window.navigator.msSaveBlob !== 'undefined') {
      window.navigator.msSaveBlob(new Blob([data]), fileName);
    } else {
      let url = window.URL.createObjectURL(new Blob([data]));
      let link = document.createElement('a');
      link.style.display = 'none';
      link.href = url;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link); //Download complete remove elements
      window.URL.revokeObjectURL(url); //releaseblobobject
    }
  });
};

/**
 * Download file used forexcelExport
 * @param url
 * @param parameter
 * @returns {*}
 */
export const getFileblob = (url, parameter) => {
  return defHttp.get(
    {
      url: url,
      params: parameter,
      responseType: 'blob',
    },
    { isTransformResponse: false }
  );
};

/**
 * 【used for评论功能】自定义File upload-method
 */
export const uploadMyFile = (url, data) => {
  return defHttp.uploadMyFile(url, data);
};
/**
 * Refresh dashboard cache
 * @param params
 */
export const refreshDragCache = () => defHttp.get({ url: Api.refreshDragCache }, { isTransformResponse: false });
/**
 * Refresh default homepage cache
 * @param params
 */
export const refreshHomeCache = () => defHttp.get({ url: Api.refreshDefaultIndexCache }, { isTransformResponse: false });
