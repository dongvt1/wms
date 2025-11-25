import { defHttp } from '/@/utils/http/axios';

enum Api {
  //Third-party login configuration
  addThirdAppConfig = '/sys/thirdApp/addThirdAppConfig',
  editThirdAppConfig = '/sys/thirdApp/editThirdAppConfig',
  getThirdConfigByTenantId = '/sys/thirdApp/getThirdConfigByTenantId',
  syncDingTalkDepartUserToLocal = '/sys/thirdApp/sync/dingtalk/departAndUser/toLocal',
  getThirdUserByWechat = '/sys/thirdApp/getThirdUserByWechat',
  wechatEnterpriseToLocal = '/sys/thirdApp/sync/wechatEnterprise/departAndUser/toLocal',
  getThirdUserBindByWechat = '/sys/thirdApp/getThirdUserBindByWechat',
  deleteThirdAccount = '/sys/thirdApp/deleteThirdAccount',
  deleteThirdAppConfig = '/sys/thirdApp/deleteThirdAppConfig',
}

/**
 * Third-party configuration saving or updating
 */
export const saveOrUpdateThirdConfig = (params, isUpdate) => {
  let url = isUpdate ? Api.editThirdAppConfig : Api.addThirdAppConfig;
  return defHttp.post({ url: url, params }, { joinParamsToUrl: true });
};

/**
 * Get third-party configuration
 * @param params
 */
export const getThirdConfigByTenantId = (params) => {
  return defHttp.get({ url: Api.getThirdConfigByTenantId, params });
};

/**
 * Synchronize DingTalk department users to local
 * @param params
 */
export const syncDingTalkDepartUserToLocal = () => {
  return defHttp.get({ url: Api.syncDingTalkDepartUserToLocal, timeout: 60000 }, { isTransformResponse: false });
};

/**
 * Obtain user information bound to Enterprise WeChat
 * @param params
 */
export const getThirdUserByWechat = () => {
  return defHttp.get({ url: Api.getThirdUserByWechat }, { isTransformResponse: false });
};

/**
 * Synchronize enterprise WeChat user departments to local
 * @param params
 */
export const wechatEnterpriseToLocal = (params) => {
  return defHttp.get({ url: Api.wechatEnterpriseToLocal, params }, { isTransformResponse: false });
};

/**
 * Get users bound to Enterprise WeChat
 * @param params
 */
export const getThirdUserBindByWechat = () => {
  return defHttp.get({ url: Api.getThirdUserBindByWechat }, { isTransformResponse: false });
};

/**
 * According to the third-party account tableidUnbind account
 * @param params
 */
export const deleteThirdAccount = (params) => {
  return defHttp.delete({ url: Api.deleteThirdAccount, params }, { isTransformResponse:false, joinParamsToUrl: true });
};

/**
 * According to the configuration tableidDelete third-party configuration
 * @param params
 * @param handleSuccess
 */
export const deleteThirdAppConfig = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteThirdAppConfig, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};