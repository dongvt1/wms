import { defHttp } from "/@/utils/http/axios";

enum Api {
  userEdit='/sys/user/login/setting/userEdit',
  getUserData='/sys/user/login/setting/getUserData',
  queryNameByCodes='/sys/position/queryByCodes',
  updateMobile='/sys/user/updateMobile',
  updateUserPassword='/sys/user/passwordChange',
  getTenantListByUserId = '/sys/tenant/getTenantListByUserId',
  cancelApplyTenant = '/sys/tenant/cancelApplyTenant',
  exitUserTenant = '/sys/tenant/exitUserTenant',
  changeOwenUserTenant = '/sys/tenant/changeOwenUserTenant',
  getThirdAccountByUserId = '/sys/thirdApp/getThirdAccountByUserId',
  bindThirdAppAccount = '/sys/thirdApp/bindThirdAppAccount',
  deleteThirdAccount = '/sys/thirdApp/deleteThirdAccount',
  agreeOrRefuseJoinTenant = '/sys/tenant/agreeOrRefuseJoinTenant',
  //Change mobile number
  changePhone = '/sys/user/changePhone',
  //User logout
  userLogOff = '/sys/user/userLogOff',
  //There is no password change request address bound to the mobile phone number.
  updatePasswordNotBindPhone = '/sys/user/updatePasswordNotBindPhone',
}

/**
 * User edit
 * @param params
 */
export const userEdit = (params) => {
  return defHttp.post({ url: Api.userEdit, params },{ isTransformResponse:false });
}

/**
 * Get user information
 * @param params
 */
export const getUserData = () => {
  return defHttp.get({ url: Api.getUserData },{ isTransformResponse:false });
}

/**
 * Get multiple job information
 * @param params
 */
export const queryNameByCodes = (params) => {
  return defHttp.get({ url: Api.queryNameByCodes, params },{isTransformResponse:false});
}

/**
 * Modify mobile phone number
 * @param params
 */
export const updateMobile = (params) => {
  return defHttp.put({ url: Api.updateMobile, params },{isTransformResponse:false});
}

/**
 * Change password
 * @param params
 */
export const updateUserPassword = (params) => {
  return defHttp.get({ url: Api.updateUserPassword, params },{isTransformResponse:false});
}

/**
 * Change password
 * @param params
 */
export const updatePasswordNotBindPhone = (params) => {
  return defHttp.put({ url: Api.updatePasswordNotBindPhone, params },{ isTransformResponse:false, joinParamsToUrl: true });
}

/**
 * by useridGet tenant list
 * @param params
 */
export const getTenantListByUserId = (params) => {
  return defHttp.get({ url: Api.getTenantListByUserId, params }, { isTransformResponse: false });
};

/**
 * Cancel application
 * @param params
 */
export const cancelApplyTenant = (params) => {
  return defHttp.put({ url: Api.cancelApplyTenant, data: params }, { joinParamsToUrl: true, isTransformResponse: false });
};

/**
 * User exits tenant
 * @param params
 */
export const exitUserTenant = (params)=>{
  return defHttp.delete({ url: Api.exitUserTenant, params },{ isTransformResponse: false, joinParamsToUrl: true });
}

/**
 * Change tenant owner
 * @param params
 */
export const changeOwenUserTenant = (params)=>{
  return defHttp.post({ url: Api.changeOwenUserTenant, params },{ isTransformResponse: false, joinParamsToUrl: true });
}

/**
 * Obtain account third-party information through third-party type
 * @param params
 */
export const getThirdAccountByUserId = (params) => {
  return defHttp.get({ url: Api.getThirdAccountByUserId, params }, { isTransformResponse: false });
};

/**
 * according to third partiesuuidBind account
 * @param params
 */
export const bindThirdAppAccount = (params) => {
  return defHttp.post({ url: Api.bindThirdAppAccount, params }, { isTransformResponse: false, joinParamsToUrl: true });
};

/**
 * according to third partiesuuidBind account
 * @param params
 */
export const deleteThirdAccount = (params) => {
  return defHttp.delete({ url: Api.deleteThirdAccount, params }, { isTransformResponse:false, joinParamsToUrl: true });
};

/**
 * Agree and refuse to join tenants
 * @param params
 */
export const agreeOrRefuseJoinTenant = (params) => {
  return defHttp.put({ url: Api.agreeOrRefuseJoinTenant, params },{ joinParamsToUrl: true });
};

/**
 * Change mobile number
 * @param params
 */
export const changePhone = (params) => {
  return defHttp.put({ url: Api.changePhone, params },{ joinParamsToUrl: true, isTransformResponse: false });
};

/**
 * User logout
 * @param params
 */
export const userLogOff = (params) => {
  return defHttp.put({ url: Api.userLogOff, params },{ isTransformResponse:false });
}
